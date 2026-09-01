package com.example.gooha.miniproject.controller;

import com.example.gooha.miniproject.dto.user.request.SignupRequestDto;
import com.example.gooha.miniproject.dto.user.response.SignupResponseDto;
import com.example.gooha.miniproject.dto.user.response.UserInfoResponseDto;
import com.example.gooha.miniproject.jwt.JwtProvider;
import com.example.gooha.miniproject.service.main.UserService;
import com.example.gooha.miniproject.service.s3.S3UploaderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserService userService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public JwtProvider jwtProvider(){
            return Mockito.mock(JwtProvider.class);
        }

        @Bean
        public S3UploaderService s3UploaderService(){
            return Mockito.mock(S3UploaderService.class);
        }
    }

    @Test
    @DisplayName("회원가입 API 성공")
    void signUp() throws Exception {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto("tester", "pass123", "tester@gmail.com");
        SignupResponseDto signupResponseDto = new SignupResponseDto("회원가입 성공", 1L, "tester");
        Mockito.when(userService.registerUser(any())).thenReturn(signupResponseDto);

        // when & then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("회원가입 성공"))
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.userName").value("tester"));
    }

    @Test
    @DisplayName("사용자 조회 API 성공")
    void findUserById() throws Exception {
        //given
        UserInfoResponseDto dto = new UserInfoResponseDto(1L, "tester", "tester@gmail.com");
        Mockito.when(userService.findUserById(1L)).thenReturn(dto);

        // when & then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.userName").value("tester"))
                .andExpect(jsonPath("$.email").value("tester@gmail.com"));
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공")
    void uploadProfileImg() throws Exception {
        //given
        MockMultipartFile file = new MockMultipartFile("file", "profile.png", MediaType.IMAGE_PNG_VALUE, "image content".getBytes());
        Mockito.when(userService.uploadProfile(any(), any())).thenReturn("https://s3.amazonaws.com/user-profile/profile.png");

        // when & then
        mockMvc.perform(multipart("/api/users/1/profile-img")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("https://s3.amazonaws.com/user-profile/profile.png"));


    }
}