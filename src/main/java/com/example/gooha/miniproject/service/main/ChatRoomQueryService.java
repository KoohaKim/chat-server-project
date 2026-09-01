package com.example.gooha.miniproject.service.main;

import com.example.gooha.miniproject.dto.chat.ChatRoomInfoResponseDto;
import com.example.gooha.miniproject.dto.chat.ChatRoomListResponseDto;
import com.example.gooha.miniproject.dto.message.response.LastMessageMetaResponseDto;
import com.example.gooha.miniproject.dto.message.response.LastMessageResponseDto;
import com.example.gooha.miniproject.repository.main.ChatRoomLastMessageRepository;
import com.example.gooha.miniproject.repository.main.ChatRoomMembersRepository;
import com.example.gooha.miniproject.sharding.MessageShardRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {
    private final ChatRoomMembersRepository chatRoomMembersRepository;
    private final ChatRoomLastMessageRepository chatRoomLastMessageRepository;
    private final MessageShardRouter messageShardRouter;

    @Cacheable(value = "myChatRooms", key = "#userId")
    @Transactional(readOnly = true, transactionManager = "mainTransactionManager")
    public List<ChatRoomListResponseDto> getMyChatRooms(Long userId) {
        // ********** 1. 내가 속한 채팅방 조회 (메인 DB) **********
        List<ChatRoomInfoResponseDto> chatRooms = chatRoomMembersRepository.getChatRoomInfo(userId);

        if (chatRooms.isEmpty()) {
            return List.of();
        }

        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoomInfoResponseDto::getId)
                .toList();

        // ********** 2. 마지막 메시지 메타 조회 (메인 DB) **********
        List<LastMessageMetaResponseDto> metas = chatRoomLastMessageRepository.findLastMessageMeta(chatRoomIds);

        if (metas.isEmpty()) {
            return chatRooms.stream()
                    .map(room -> ChatRoomListResponseDto.from(room, null))
                    .toList();
        }

        // ********** 3. chatRoomId -> meta 매핑 **********
        Map<Long, LastMessageMetaResponseDto> metaByRoomId = metas.stream()
                .collect(Collectors.toMap(
                        LastMessageMetaResponseDto::getChatRoomId,
                        Function.identity()
                ));

        // ********** 4. shardKey → messageIds 그룹핑 **********
        Map<Integer, List<Long>> shardToMessagesIds = new HashMap<>();
        for (LastMessageMetaResponseDto meta : metas) {
            shardToMessagesIds
                    .computeIfAbsent(meta.getShardKey(), k -> new ArrayList<>())
                    .add(meta.getMessageId());
        }

        // ********** 5. 샤드별 메시지 조회 (메시지 DB) **********
        Map<Long, LastMessageResponseDto> messageMap = new HashMap<>();

        shardToMessagesIds.forEach((shardKey, messageIds) -> {
            if (messageIds.isEmpty()) {
                return;
            }
            List<LastMessageResponseDto> messages = messageShardRouter.findMessage(shardKey, messageIds);
            for (LastMessageResponseDto msg : messages) {
                messageMap.put(msg.getMessageId(), msg);
            }
        });

        // ********** 6. 최종 DTO 조립 **********
        return chatRooms.stream()
                .map(room -> {
                    LastMessageMetaResponseDto meta = metaByRoomId.get(room.getId());
                    LastMessageResponseDto message = meta == null ? null : messageMap.get(meta.getMessageId());

                    return new ChatRoomListResponseDto(
                            room.getId(),
                            room.getName(),
                            room.isGroup(),
                            message == null ? null : message.getContent(),
                            message == null ? null : Timestamp.from(message.getCreatedAt().toInstant())
                    );
                })
                .toList();
    }
}