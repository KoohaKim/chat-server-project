package com.example.gooha.miniproject.service.message;

import com.example.gooha.miniproject.dto.message.ChatMessageEvent;
import com.example.gooha.miniproject.dto.message.response.MessageResponseDto;
import com.example.gooha.miniproject.entity.message.Message;
import com.example.gooha.miniproject.kafka.KafkaChatProducer;
import com.example.gooha.miniproject.repository.message.MessageRepository;
import com.example.gooha.miniproject.sharding.MessageShardKeySelector;
import com.example.gooha.miniproject.sharding.ShardContext;
import com.example.gooha.miniproject.sharding.Sharding;
import com.example.gooha.miniproject.sharding.ShardingTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StopWatch;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    private final MessageRepository messageRepository;
    private final MessageWriteService messageWriteService;
//    private final LastMessageService lastMessageService;
    private final ChatLastMessageDebouncer chatLastMessageDebouncer;
    private final MessageShardKeySelector messageShardKeySelector;


    public Long sendMessage(Long chatRoomId, Long senderId, String content) {
        Integer shardKey = messageShardKeySelector.getShardKey(chatRoomId);
        ShardContext.setShardKey(shardKey);

        try {
            Long messageId = messageWriteService.saveMessage(chatRoomId, senderId, content);
            chatLastMessageDebouncer.onNewMessage(chatRoomId, messageId, shardKey);

            return messageId;
        } finally {
            ShardContext.clear();
        }
    }



    @Sharding(target = ShardingTarget.MESSAGE)
    @Transactional(readOnly = true)
    public Page<MessageResponseDto> findAllMessage(Long chatRoomId, Pageable pageable) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Page<Message> page = messageRepository.findAll(pageable);
        Page<MessageResponseDto> map = page.map(MessageResponseDto::new);

        stopWatch.stop();
        System.out.println("findAllmessage 쿼리 수행시간 : " +stopWatch.getTotalTimeMillis());
        return map;
    }


    @Transactional()
    public void makeTestMessages() {
        for (int i = 0; i < 100000; i++) {
            Message message = new Message();
            message.setSenderId(999L);
            message.setContent("test입니다");
            message.setChatRoomId(10L);
            messageRepository.save(message);
        }
    }


}

