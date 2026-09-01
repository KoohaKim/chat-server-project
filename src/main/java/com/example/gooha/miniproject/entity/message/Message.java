package com.example.gooha.miniproject.entity.message;

import com.example.gooha.miniproject.entity.time.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "messages")
@Entity
public class Message extends BaseTimeEntity {
    @Id
    private Long id;

    @Column(name = "chat_room_id")
    @NotNull
    private Long chatRoomId;

    @Column(name = "sender_id")
    @NotNull
    private Long senderId;

    @Column(name = "content")
    @NotNull
    private String content;

}
