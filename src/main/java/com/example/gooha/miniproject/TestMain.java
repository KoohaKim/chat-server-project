package com.example.gooha.miniproject;

import java.util.concurrent.atomic.AtomicInteger;

public class TestMain {
    public static void main(String[] args) {
        AtomicInteger v = new AtomicInteger(1);
        int vv = v.incrementAndGet();
    }
}
