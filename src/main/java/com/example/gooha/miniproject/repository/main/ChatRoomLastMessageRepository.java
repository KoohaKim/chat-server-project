package com.example.gooha.miniproject.repository.main;

import com.example.gooha.miniproject.dto.message.response.LastMessageMetaResponseDto;
import com.example.gooha.miniproject.entity.main.ChatRoomLastMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface ChatRoomLastMessageRepository extends JpaRepository<ChatRoomLastMessage, Long> {

    @Modifying
    @Query(
            value = """
        INSERT INTO chat_room_last_message (
            chat_room_id,
            message_id,
            shard_key,
            modified_at
        )
        VALUES (
            :chatRoomId,
            :messageId,
            :shardKey,
            :modifiedAt
        )
        ON DUPLICATE KEY UPDATE
            message_id = VALUES(message_id),
            shard_key = VALUES(shard_key),
            modified_at = VALUES(modified_at)
        """,
            nativeQuery = true
    )
    void updateLastMessage(@Param("chatRoomId") Long chatRoomId,
                           @Param("messageId") Long messageId,
                           @Param("shardKey") Integer shardKey,
                           @Param("modifiedAt") ZonedDateTime modifiedAt);



    @Query("""
        SELECT new com.example.gooha.miniproject.dto.message.response.LastMessageMetaResponseDto(
            lm.chatRoomId,
            lm.messageId,
            lm.shardKey
        )
        FROM ChatRoomLastMessage lm
        WHERE lm.chatRoomId IN :chatRoomIds
    """)
    List<LastMessageMetaResponseDto> findLastMessageMeta(@Param("chatRoomIds")List<Long> chatRoomIds);
}
