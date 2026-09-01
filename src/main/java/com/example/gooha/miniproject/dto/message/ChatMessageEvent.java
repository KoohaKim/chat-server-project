package com.example.gooha.miniproject.dto.message;


public record ChatMessageEvent(Long messageId, Long chatRoomId, Long senderId, String content) {
}