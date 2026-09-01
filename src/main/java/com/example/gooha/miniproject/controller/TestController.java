package com.example.gooha.miniproject.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public String test() throws InterruptedException {
        Thread.sleep(2000);
        return "sdf";
        // throw new RuntimeException("erererer");
        // return "okok";
    }
}
