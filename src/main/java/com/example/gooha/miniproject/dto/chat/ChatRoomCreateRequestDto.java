package com.example.gooha.miniproject.dto.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomCreateRequestDto {
    private String roomName;
    private int roomOwnerId;
}
