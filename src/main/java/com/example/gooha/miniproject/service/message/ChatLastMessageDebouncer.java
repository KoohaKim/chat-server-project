package com.example.gooha.miniproject.service.message;

import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatLastMessageDebouncer {
    private final RedissonClient redissonClient;
    private static final long DEBOUNCE_DELAY_SEC = 3; // 디바운스 구간


    public void onNewMessage(Long roomId, Long messageId, Integer shardKey) {
        RMap<Long, String> pendingMap = redissonClient.getMap("chat:lastmsg:pending");
        RBucket<Boolean> scheduledFlag = redissonClient.getBucket("chat:lastmsg:scheduled:" + roomId);

        pendingMap.put(roomId, messageId + ":" + shardKey);

        if(!Boolean.TRUE.equals(scheduledFlag.get())) { //null or true

            //Redis: 'chat:lastmsg:scheduled:7 = true ....3초후...삭제.'
            scheduledFlag.set(true, DEBOUNCE_DELAY_SEC, TimeUnit.SECONDS);

            RBlockingQueue<Long> destinationQueue =
                    redissonClient.getBlockingQueue("chat:lastmsg:flush:queue");
            RDelayedQueue<Long> delayedQueue = redissonClient.getDelayedQueue(destinationQueue);
            delayedQueue.offer(roomId, DEBOUNCE_DELAY_SEC, TimeUnit.SECONDS);
        }
    }
}
