package com.example.gooha.miniproject.controller;

import com.example.gooha.miniproject.dto.user.request.LoginRequestDto;
import com.example.gooha.miniproject.dto.user.request.SignupRequestDto;
import com.example.gooha.miniproject.dto.user.response.LoginResponseDto;
import com.example.gooha.miniproject.dto.user.response.SignupResponseDto;
import com.example.gooha.miniproject.dto.user.response.UserInfoResponseDto;
import com.example.gooha.miniproject.service.main.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signUp(@RequestBody SignupRequestDto signupRequestDto) {
        SignupResponseDto response = userService.registerUser(signupRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> logIn(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto response = userService.loginUser(loginRequestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserInfoResponseDto> findUserById(@PathVariable Long id) {
        UserInfoResponseDto response = userService.findUserById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/profile-img")
    public ResponseEntity<String> uploadProfileImg(@PathVariable Long id,
                                                   @RequestParam("file") MultipartFile file) {
        String imgUrl = userService.uploadProfile(id, file);
        return ResponseEntity.ok(imgUrl);
    }

}
