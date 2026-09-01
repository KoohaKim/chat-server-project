package com.example.gooha.miniproject.dto.message.response;

import com.example.gooha.miniproject.entity.message.Message;
import lombok.Getter;

@Getter
public class MessageResponseDto {
    private final Long id;
    private final Long senderId;
    private final String content;

    public MessageResponseDto(Message message) {
        this.id = message.getId();
        this.senderId = message.getSenderId();
        this.content = message.getContent();
    }
}
