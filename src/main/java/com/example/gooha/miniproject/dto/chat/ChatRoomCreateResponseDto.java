package com.example.gooha.miniproject.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomCreateResponseDto {
    private Long chatRoomId;
    private String roomName;
    private Long roomOwnerId;
}
