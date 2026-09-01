package com.example.gooha.miniproject.service;

import com.example.gooha.miniproject.dto.user.request.LoginRequestDto;
import com.example.gooha.miniproject.dto.user.request.SignupRequestDto;
import com.example.gooha.miniproject.dto.user.response.LoginResponseDto;
import com.example.gooha.miniproject.dto.user.response.SignupResponseDto;
import com.example.gooha.miniproject.dto.user.response.UserInfoResponseDto;
import com.example.gooha.miniproject.repository.main.UserRepository;
import com.example.gooha.miniproject.service.main.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")

class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
    }


    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("회원가입 성공")
    void registerUser() {
        //given
        SignupRequestDto dto = new SignupRequestDto("tester", "pass123", "tester@email.com");

        //when
        SignupResponseDto signupResponseDto = userService.registerUser(dto);

        UserInfoResponseDto userById = userService.findUserById(signupResponseDto.getUserId());

        //then
        assertEquals(signupResponseDto.getUserId(), userById.getUserId(), "userId가 일치하지 않습니다.");
        assertEquals(signupResponseDto.getUserName(), userById.getUserName(), "userName이 일치하지 않습니다.");
    }

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("로그인 성공")
    void login_Success(){
        //given
        SignupRequestDto dto = new SignupRequestDto("tester", "pass123", "tester@email.com");
        userService.registerUser(dto);

        LoginRequestDto loginDto = new LoginRequestDto("tester", "pass123");

        //when
        LoginResponseDto loginResponseDto = userService.loginUser(loginDto);

        //then
        assertEquals("tester", loginResponseDto.getUserName(),"userName이 일치하지 않습니다.");
        assertEquals("Bearer", loginResponseDto.getTokenType(),"token 타입이 Bearer이 아닙니다.");
        assertNotNull(loginResponseDto,"token이 null입니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 유저")
    void loginFail_nonexistentUser() {
        //given
        SignupRequestDto dto = new SignupRequestDto("tester", "pass123", "tester@email.com");
        userService.registerUser(dto);

        //when
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.loginUser(new LoginRequestDto("none", "pass"))
        );

        //then
        assertEquals("존재하지 않는 사용자 입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 오류")
    void loginUser_wrongPassword() {
        //given
        userService.registerUser(new SignupRequestDto("tester", "correctPass", "tester@email.com"));
        LoginRequestDto loginDto = new LoginRequestDto("tester", "wrongPass");

        //when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.loginUser(loginDto));

        //then
        assertEquals("비밀번호가 올바르지 않습니다.", exception.getMessage());
    }


    @Test
    @DisplayName("사용자 정보 조회")
    void findUserById_success() {
        // given
        SignupRequestDto dto = new SignupRequestDto("tester", "pass", "test@email.com");
        SignupResponseDto signupResponse = userService.registerUser(dto);

        // when
        UserInfoResponseDto info = userService.findUserById(signupResponse.getUserId());

        // then
        assertEquals("tester", info.getUserName());
        assertEquals("test@email.com", info.getEmail());
    }

    @Test
    @DisplayName("사용자 정보 조회 실패")
    void findUserById_notFound() {
        // then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.findUserById(9999L));

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공")
    void uploadProfile_Success(){
        //given
        SignupResponseDto user = userService.registerUser(new SignupRequestDto("tester", "pass", "test@email.com"));

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "dummy".getBytes()
        );

        //when
        String imgUrl = userService.uploadProfile(user.getUserId(), mockFile);

        //then
        assertNotNull(imgUrl);
    }

}