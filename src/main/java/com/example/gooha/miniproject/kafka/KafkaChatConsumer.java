package com.example.gooha.miniproject.kafka;

import com.example.gooha.miniproject.ws.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaChatConsumer {
    private final SessionManager sessionManager;

    @KafkaListener(topics = "chat-message-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String payload, Acknowledgment ack) {
        try {
            log.info("[Kafka] 컨슈머가 메시지 수신 함. 수동 처리 합니다. payload {}", payload);

            String[] parts = payload.split("\\|",4);

            if (parts.length < 4) {
                log.error("[Kafka] 올바르지 않은 메시지 포맷입니다. 패스합니다. payload: {}", payload);
                ack.acknowledge();
                return;
            }

            Long messageId = Long.parseLong(parts[0]);
            Long chatRoomId = Long.parseLong(parts[1]);
            Long senderId = Long.parseLong(parts[2]);
            String content = parts[3];

            String formatted = String.format("[Room %d] User %d: %s", chatRoomId, senderId, content);
            sessionManager.broadcast(chatRoomId, formatted);

            // 성공 시에만 수동 커밋
            ack.acknowledge();
            log.info("[Kafka] 웹소켓 브로드캐스트 및 수동 커밋 완료 chatRoomId: {}", chatRoomId);

        } catch (NumberFormatException nfe) {
            log.error("[Kafka] 데이터 파싱 중 숫자 변환 오류 발생 (포맷 불일치). 무한 재처리를 막기 위해 강제 커밋합니다.", nfe);
            ack.acknowledge(); // 포맷 에러일 때는 패스
        } catch (Exception e){
            // ack.acknowledge 가 실행안되거임 -> 컨슈머 재시작 or 에러 해결 시 메시지 재처리보장
            log.error("Kafka 메시지 처리 또는 웹소켓 브로드캐스트 실패 (커밋 안 됨, 재처리 대기)", e);
            throw e;
        }
    }
}
