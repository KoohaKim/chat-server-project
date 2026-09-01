package com.example.gooha.miniproject.service.main;

import com.example.gooha.miniproject.dto.user.request.LoginRequestDto;
import com.example.gooha.miniproject.dto.user.request.SignupRequestDto;
import com.example.gooha.miniproject.dto.user.response.LoginResponseDto;
import com.example.gooha.miniproject.dto.user.response.MyProfileResponseDto;
import com.example.gooha.miniproject.dto.user.response.SignupResponseDto;
import com.example.gooha.miniproject.dto.user.response.UserInfoResponseDto;
import com.example.gooha.miniproject.entity.main.User;
import com.example.gooha.miniproject.jwt.JwtProvider;
import com.example.gooha.miniproject.repository.main.UserRepository;
import com.example.gooha.miniproject.service.s3.S3UploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String DIR_NAME_USER_PROFILE = "user-profile";

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final S3UploaderService s3UploaderService;

    @Transactional
    public SignupResponseDto registerUser(SignupRequestDto signupRequestDto) {
        User user = new User();
        user.setUserName(signupRequestDto.getUserName());
        user.setPassword(signupRequestDto.getPassword());
        user.setEmail(signupRequestDto.getEmail());
        user.setRole("USER");

        userRepository.save(user);

        return new SignupResponseDto("회원가입 성공", user.getId(), user.getUserName());
    }

    @Transactional
    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUserName(loginRequestDto.getUserName())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자 입니다."));

        if (!user.getPassword().equals(loginRequestDto.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        String token = jwtProvider.generateToken(user.getUserName(), user.getId(), user.getRole());

        return new LoginResponseDto(token, "Bearer", user.getUserName());
    }


    @Transactional(readOnly = true)
    public UserInfoResponseDto findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new UserInfoResponseDto(user.getId(), user.getUserName(), user.getEmail());
    }


    @Transactional
    public String uploadProfile(Long id, MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        try {
            String imgUrl = s3UploaderService.upload(file, DIR_NAME_USER_PROFILE);

            user.setProfile(imgUrl);

            return imgUrl;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 내 프로필 조회
    @Transactional(readOnly = true)
    public MyProfileResponseDto viewMyProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        return new MyProfileResponseDto(user);
    }


}
