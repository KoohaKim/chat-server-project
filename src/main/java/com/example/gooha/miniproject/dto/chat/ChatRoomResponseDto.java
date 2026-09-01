package com.example.gooha.miniproject.dto.chat;

import com.example.gooha.miniproject.entity.main.ChatRoom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class ChatRoomResponseDto {
    private final Long id;
    private final String name;
    private final Boolean isGroup;

    public ChatRoomResponseDto(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.name = chatRoom.getName();
        this.isGroup = chatRoom.isGroup();
    }
}
