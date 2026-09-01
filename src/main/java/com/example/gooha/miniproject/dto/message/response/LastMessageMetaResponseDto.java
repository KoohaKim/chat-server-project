package com.example.gooha.miniproject.dto.message.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@RequiredArgsConstructor
public class LastMessageMetaResponseDto {
    private final Long ChatRoomId;
    private final Long MessageId;
    private final Integer ShardKey;
}
