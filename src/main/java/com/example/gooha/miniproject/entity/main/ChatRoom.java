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
@Table(name = "chat_rooms")
@Entity
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "is_group")
    private boolean isGroup;

    @Column(name = "room_owner_id")
    @NotNull
    private Long roomOwnerId;


}
