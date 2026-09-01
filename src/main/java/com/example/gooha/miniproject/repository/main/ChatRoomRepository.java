package com.example.gooha.miniproject.repository.main;

import com.example.gooha.miniproject.entity.main.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT m.userId FROM ChatRoomMembers m WHERE m.chatRoomId = :chatRoomId")
    List<Long> findUserIdsByChatroomId(@Param("chatRoomId") Long chatRoomId);

}
