package com.example.gooha.miniproject.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponseDto {
    private Long userId;
    private String userName;
    private String email;
}
