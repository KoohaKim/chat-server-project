package com.example.gooha.miniproject.kafka;

import com.example.gooha.miniproject.dto.message.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageTransactionalListener {
    private final KafkaChatProducer kafkaChatProducer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatMessageCommit(ChatMessageEvent event) {
        log.info("[messageTransactionManager] DB 트랜잭션 정상 커밋 완료 -> 카프카 이벤트 발행. ChatRoomId: {}" , event.chatRoomId());

        try{
            kafkaChatProducer.sendMessage(
                    event.messageId(),
                    event.chatRoomId(),
                    event.senderId(),
                    event.content()
            );
        } catch (Exception e) {
            log.error("카프카 발행 실패! (유실 방지를 위한 재처리 대책 필요) - Message ID: {}", event.messageId(), e);
        }

    }

}
