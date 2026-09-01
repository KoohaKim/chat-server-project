package com.example.gooha.miniproject.dto.user.response;

import com.example.gooha.miniproject.entity.main.User;
import lombok.Getter;


@Getter
public class MyProfileResponseDto {
    private final Long id;
    private final String userName;
    private final String email;
    private final String profileImageUrl;

    public MyProfileResponseDto(User user) {
        this.id = user.getId();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.profileImageUrl = user.getProfile();
    }
}
