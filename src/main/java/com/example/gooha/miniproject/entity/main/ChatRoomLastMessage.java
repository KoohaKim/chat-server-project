package com.example.gooha.miniproject.entity.main;

import com.example.gooha.miniproject.entity.time.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_room_last_message")
public class ChatRoomLastMessage extends BaseTimeEntity {
    @Id
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "shard_key", nullable = false)
    private Integer shardKey;


}
