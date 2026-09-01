package com.example.gooha.miniproject.dto.message.request;

import lombok.Data;

@Data
public class MessageRequestDto {
    private Long chatRoomId;
    private Long senderId;
    private String content;
}
