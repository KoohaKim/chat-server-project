package com.example.gooha.miniproject.repository.main;

import com.example.gooha.miniproject.dto.chat.ChatRoomInfoResponseDto;
import com.example.gooha.miniproject.entity.main.ChatRoomMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMembersRepository extends JpaRepository<ChatRoomMembers, Long> {

    @Query("SELECT m FROM ChatRoomMembers m " +
            "WHERE m.userId = :userId " +
            "AND m.chatRoomId = :chatRoomId")
    Optional<ChatRoomMembers> findExistMember(@Param("userId") Long userId, @Param("chatRoomId") Long chatRoomId);

    @Query("""
                SELECT new com.example.gooha.miniproject.dto.chat.ChatRoomInfoResponseDto(
                    c.id,
                    c.name,
                    c.isGroup
                )
                FROM ChatRoomMembers crm, ChatRoom c
                WHERE crm.chatRoomId = c.id
                  AND crm.userId = :userId
                  AND crm.deletedAt IS NULL
            """)
    List<ChatRoomInfoResponseDto> getChatRoomInfo(@Param("userId") Long userId);

    // 채팅방에 속한 모든 유저 ID 조회 (캐시 무효화용)
    @Query("SELECT m.userId FROM ChatRoomMembers m WHERE m.chatRoomId = :chatRoomId AND m.deletedAt IS NULL")
    List<Long> findUserIdsByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}