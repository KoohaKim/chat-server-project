package com.example.gooha.miniproject.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
class JwtProviderTest {
    @Autowired
    private JwtProvider jwtProvider;

    @Test
    void getLongToken(){
        String token = jwtProvider.generateMasterToken("Kim", 1L, "USER");
        System.out.println("토큰 값: " + token);
    }

}