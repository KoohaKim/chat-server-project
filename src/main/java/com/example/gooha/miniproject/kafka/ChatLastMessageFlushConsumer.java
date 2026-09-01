package com.example.gooha.miniproject.kafka;

import com.example.gooha.miniproject.service.message.LastMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatLastMessageFlushConsumer {

    private final RedissonClient redissonClient;
    private final LastMessageService lastMessageService;

    private static final long DEBOUNCE_DELAY_SEC = 3;
    private volatile boolean running = true;


    @PostConstruct
    public void start() {
        Thread consumerThread = new Thread(this::consumeLoop, "lastmsg-flush-consumer");
        consumerThread.setDaemon(true);//main 스레드 종료시 같이종료
        consumerThread.start();
        log.info("[LastMsgFlush] 컨슈머 스레드 시작");
    }


    private void consumeLoop() {
        RBlockingQueue<Long> destinationQueue = redissonClient.getBlockingQueue("chat:lastmsg:flush:queue");

        while (running) {
            try {
                Long roomId = destinationQueue.poll(1, TimeUnit.SECONDS);
                if (roomId == null) continue;
                flush(roomId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // 예외가 나도 루프는 유지 (특정 roomId 실패가 전체를 멈추면 안 됨)
                log.error("[LastMsgFlush] flush 처리 중 예외 발생", e);
            }
        }
        log.info("[LastMsgFlush] 컨슈머 스레드 종료");
    }


    private void flush(Long roomId) {
        RMap<Long, String> pendingMap = redissonClient.getMap("chat:lastmsg:pending");

        String value = pendingMap.remove(roomId);
        if (value == null) {
            log.warn("[LastMsgFlush] pendingMap에 값 없음 - 이미 처리됐거나 누락. roomId={}", roomId);
            return;
        }

        String[] parts = value.split(":");
        Long messageId = Long.parseLong(parts[0]);
        Integer shardKey = Integer.parseInt(parts[1]);

        try {
            lastMessageService.updateLastMessage(roomId, messageId, shardKey);
            log.info("[LastMsgFlush] DB flush 완료 roomId={}, messageId={}, shardKey={}", roomId, messageId, shardKey);
        } catch (Exception e) {
            log.error("[LastMsgFlush] DB flush 실패 roomId={}, messageId={}", roomId, messageId, e);
            pendingMap.putIfAbsent(roomId, value);
            reschedule(roomId);
            return;
        }

        if(pendingMap.containsKey(roomId)) {
            log.info("[LastMsgFlush] flush 도중 새 메시지 도착 감지, 재스케줄. roomId={}", roomId);
            reschedule(roomId);
        }
    }


    private void reschedule(Long roomId) {
        RBucket<Boolean> scheduledFlag =
                redissonClient.getBucket("chat:lastmsg:scheduled:" + roomId);

        if (!Boolean.TRUE.equals(scheduledFlag.get())) {
            scheduledFlag.set(true, DEBOUNCE_DELAY_SEC, TimeUnit.SECONDS);

            RBlockingQueue<Long> destinationQueue =
                    redissonClient.getBlockingQueue("chat:lastmsg:flush:queue");
            redissonClient.getDelayedQueue(destinationQueue)
                    .offer(roomId, DEBOUNCE_DELAY_SEC, TimeUnit.SECONDS);
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        log.info("[LastMsgFlush] 컨슈머 종료 신호 전송");
    }



}



