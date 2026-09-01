package com.example.gooha.miniproject;

public class ThreadPoolMain {
    public static void main(String[] args) {
        for (int i = 0; i < 100; i++){
            new Thread(() -> {
                while(true) {

                }
            }).start();
        }
    }
}
