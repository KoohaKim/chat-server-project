package com.example.gooha.miniproject.service.main;

import com.example.gooha.miniproject.dto.message.ChatMessageEvent;
import com.example.gooha.miniproject.repository.main.ChatRoomMembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomCacheEvictService {
    private final CacheManager cacheManager;
    private final ChatRoomMembersRepository chatRoomMembersRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageEvent(ChatMessageEvent event) {
        evictChatRoomCache(event.chatRoomId());
    }

    public void evictChatRoomCache(Long chatRoomId) {
        List<Long> memberIds = chatRoomMembersRepository.findUserIdsByChatRoomId(chatRoomId);

        Cache cache = cacheManager.getCache("myChatRooms");
        if (cache != null) {
            memberIds.forEach(userId -> {
                cache.evict(userId);
                log.info("캐시 무효화: userId={}, chatRoomId={}", userId, chatRoomId);
            });
        }
    }
}