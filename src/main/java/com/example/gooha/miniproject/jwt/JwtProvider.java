package com.example.gooha.miniproject.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {
    @Value("${spring.jwt.secret}")
    private String SECRET_KEY ;
    private final long EXPIRATION_TIME = 1000L * 60 * 60 * 24; // 1일
    private final long ONE_HUNDRED_YEARS = 1000L * 60 * 60 * 24 * 365 * 100;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, Long userId, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role",role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 💡 K6 부하 테스트나 로컬 개발용 100년짜리 마스터 토큰 생성
    public String generateMasterToken(String username, Long userId, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                // 현재 시간 + 100년 설정
                .setExpiration(new Date(System.currentTimeMillis() + ONE_HUNDRED_YEARS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }



    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public String getUserRole(String token) {
        return parseClaims(token).get("role", String.class);
    }


    public String getUsernameByToken(String token){
        return parseClaims(token).getSubject();
    }

    public Long getUserIdByToken(String token){
        String pureToken = token.replace("Bearer ", "").trim();

        return parseClaims(pureToken).get("userId", Long.class);
    }


    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            System.out.println("토큰 검증 통과: " + token);
            return true;
        } catch (JwtException | IllegalArgumentException e){
            System.out.println("토큰 검증 실패: " + token);
            e.printStackTrace();
            return false;
        }
    }

}
