package com.example.gooha.miniproject.sharding;

import org.springframework.stereotype.Component;

@Component
public class MessageShardKeySelector {

    public Integer getShardKey(Long chatRoomId) {
        if(chatRoomId == null) {
            return 0;
        }
        return Math.toIntExact(chatRoomId % 2);
    }


}
