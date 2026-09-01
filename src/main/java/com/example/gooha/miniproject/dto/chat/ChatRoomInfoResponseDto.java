package com.example.gooha.miniproject.dto.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@RequiredArgsConstructor
public class ChatRoomInfoResponseDto {
    private final Long id;
    private final String name;
    private final boolean isGroup;
}
