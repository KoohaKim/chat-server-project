package com.example.gooha.miniproject.dto.chat;

import com.example.gooha.miniproject.dto.message.response.LastMessageResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListResponseDto {
    private Long chatRoomId;
    private String chatRoomName;

    @JsonProperty("isGroup")  // boolean 직렬화 이슈 방지
    private boolean isGroup;
    private String lastMessage;
    private ZonedDateTime lastMessageAt;

    public ChatRoomListResponseDto(Long chatRoomId,
                                   String chatRoomName,
                                   boolean isGroup,
                                   String lastMessage,
                                   Timestamp lastMessageAt) {
        this.chatRoomId = chatRoomId;
        this.chatRoomName = chatRoomName;
        this.isGroup = isGroup;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt == null
                ? null
                : lastMessageAt.toInstant().atZone(ZoneId.of("UTC"));
    }

    public static ChatRoomListResponseDto from(
            ChatRoomInfoResponseDto room,
            LastMessageResponseDto message
    ) {
        return new ChatRoomListResponseDto(
                room.getId(),
                room.getName(),
                room.isGroup(),
                message == null ? null : message.getContent(),
                message == null ? null
                        : Timestamp.from(message.getCreatedAt().toInstant())
        );
    }


}