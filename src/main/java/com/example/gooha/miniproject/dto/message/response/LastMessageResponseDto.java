package com.example.gooha.miniproject.dto.message.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;

@ToString
@Getter
@RequiredArgsConstructor
public class LastMessageResponseDto {
    private final Long messageId;
    private final String content;
    private final ZonedDateTime createdAt;
}
