package com.example.gooha.miniproject.dto.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public class ChatRoomMembersResponseDto {
    private Set<String> members;

    public ChatRoomMembersResponseDto(Set<String> members) {
        this.members = members;
    }
}
