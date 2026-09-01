package com.example.gooha.miniproject.controller;

import com.example.gooha.miniproject.service.s3.S3PresignerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
public class S3PresignController {
    private final S3PresignerService s3PresignerService;

    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(@RequestParam("key") String key) {
        String url = s3PresignerService.generatePresignedUrl(key, Duration.ofMinutes(15));
        return ResponseEntity.ok(url);
    }
}
