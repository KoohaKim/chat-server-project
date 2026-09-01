package com.example.gooha.miniproject.service.message;

import com.example.gooha.miniproject.dto.message.ChatMessageEvent;
import com.example.gooha.miniproject.entity.message.Message;
import com.example.gooha.miniproject.repository.message.MessageRepository;
import com.example.gooha.miniproject.sharding.Sharding;
import com.example.gooha.miniproject.sharding.ShardingTarget;
import com.example.gooha.miniproject.sharding.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageWriteService {
    private final MessageRepository messageRepository;
    private final Snowflake snowflake;
    private final ApplicationEventPublisher eventPublisher;

    @Sharding(target = ShardingTarget.MESSAGE)
    @Transactional("messageTransactionManager")
    public Long saveMessage(Long chatRoomId, Long senderId, String content) {
        Long messageId = snowflake.nextId();

        Message message = Message.builder()
                .id(messageId)
                .chatRoomId(chatRoomId)
                .senderId(senderId)
                .content(content)
                .build();

        messageRepository.save(message);

        // 이벤트 발행 → ChatRoomCacheEvictService에서 캐시 무효화 처리
        eventPublisher.publishEvent(new ChatMessageEvent(messageId, chatRoomId, senderId, content));

        return messageId;
    }
}