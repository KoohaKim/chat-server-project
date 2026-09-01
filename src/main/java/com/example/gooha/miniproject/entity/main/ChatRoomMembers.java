package com.example.gooha.miniproject.entity.main;

import com.example.gooha.miniproject.entity.time.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@Table(name = "chat_room_members")
@Entity
public class ChatRoomMembers extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    @NotNull
    private Long userId;

    @Column(name = "chat_room_id")
    @NotNull
    private Long chatRoomId;

}
