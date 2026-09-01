package com.example.gooha.miniproject.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponseDto {
    private String message;
    private Long userId;
    private String userName;
}
