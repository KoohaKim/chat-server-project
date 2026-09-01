package com.example.gooha.miniproject.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomMemberCacheService {
    private final StringRedisTemplate stringRedisTemplate;

    public void addUserToRoom(Long chatRoomId, String userId) {
        String key = getKey(chatRoomId);
        stringRedisTemplate.opsForSet().add(key, userId);
        log.info("Redis 유저 등록: key(chatRoom)={}, userId={}", key, userId);
    }

    public void removeUserFromRoom(Long chatRoomId, String userId) {
        String key = getKey(chatRoomId);
        stringRedisTemplate.opsForSet().remove(key, userId);
        log.info("Redis에서 유저 제거: key(chatRoom)={}, userId={}", key, userId);
    }

    public Set<String> getAllUsersInRoom(Long chatRoomId) {
        String key = getKey(chatRoomId);
        return stringRedisTemplate.opsForSet().members(key);
    }

    private String getKey(Long chatRoomId) {
        return "chatRoom:" + chatRoomId;
    }

}
