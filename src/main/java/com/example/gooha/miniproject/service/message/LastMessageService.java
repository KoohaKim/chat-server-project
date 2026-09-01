package com.example.gooha.miniproject.service.message;

import com.example.gooha.miniproject.repository.main.ChatRoomLastMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class LastMessageService {
    private final ChatRoomLastMessageRepository lastMessageRepository;

    @Transactional("mainTransactionManager")
    public void updateLastMessage(Long chatRoomId, Long messageId, Integer shardKey){
        lastMessageRepository.updateLastMessage(
                chatRoomId,
                messageId,
                shardKey,
                ZonedDateTime.now()
        );
    }
}
