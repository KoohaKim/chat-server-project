package com.example.gooha.miniproject.service.main;

import com.example.gooha.miniproject.dto.chat.ChatRoomInfoResponseDto;
import com.example.gooha.miniproject.dto.chat.ChatRoomMembersResponseDto;
import com.example.gooha.miniproject.dto.chat.ChatRoomResponseDto;
import com.example.gooha.miniproject.dto.chat.ChatRoomListResponseDto;
import com.example.gooha.miniproject.dto.message.response.LastMessageMetaResponseDto;
import com.example.gooha.miniproject.dto.message.response.LastMessageResponseDto;
import com.example.gooha.miniproject.entity.main.ChatRoom;
import com.example.gooha.miniproject.entity.main.ChatRoomMembers;
import com.example.gooha.miniproject.redis.ChatRoomMemberCacheService;
import com.example.gooha.miniproject.repository.main.ChatRoomLastMessageRepository;
import com.example.gooha.miniproject.repository.main.ChatRoomMembersRepository;
import com.example.gooha.miniproject.repository.main.ChatRoomRepository;
import com.example.gooha.miniproject.repository.message.MessageRepository;
import com.example.gooha.miniproject.sharding.MessageShardRouter;
import com.example.gooha.miniproject.sharding.ShardContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMembersRepository chatRoomMembersRepository;
    private final ChatRoomLastMessageRepository chatRoomLastMessageRepository;
    private final MessageShardRouter messageShardRouter;
    private final ChatRoomMemberCacheService chatRoomMemberCacheService;

    private final MessageRepository messageRepository;

    // 대화방 생성
    @Transactional
    public ChatRoom createChatRoom(String roomName, long room_owner_id) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(roomName);
        chatRoom.setRoomOwnerId(room_owner_id);
        return chatRoomRepository.save(chatRoom);
    }


    // 단일 대화방 조회
    @Transactional(readOnly = true)
    public ChatRoomResponseDto findChatRoomById(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 챗룸 입니다."));

        return new ChatRoomResponseDto(chatRoom);
    }

    // 모든 대화방 조회
    @Transactional(readOnly = true)
    public Page<ChatRoomResponseDto> findAllChatRoom(Pageable pageable) {
        Page<ChatRoom> page = chatRoomRepository.findAll(pageable);
        return page.map(ChatRoomResponseDto::new);
    }


    // (Redis) 현재 방 멤버 조회
    @Transactional(readOnly = true)
    public ChatRoomMembersResponseDto findChatRoomMembers(Long chatRoomId) {
        Set<String> allUsersInRoom = chatRoomMemberCacheService.getAllUsersInRoom(chatRoomId);

        if (allUsersInRoom == null) {
            allUsersInRoom = Collections.emptySet();
        }

        return new ChatRoomMembersResponseDto(allUsersInRoom);
    }

    // 채팅방 입장
    @Transactional
    public void enterChatRoom(Long userId, Long chatRoomId) {
        Optional<ChatRoomMembers> optional = chatRoomMembersRepository.findExistMember(userId, chatRoomId);

        if (optional.isPresent()) {
            ChatRoomMembers chatRoomMembers = optional.get();

            // isDeleted == false == null -> 이미 active
            if (!chatRoomMembers.isDeleted()) {
                log.info("이미 active 된 유저입니다.");
                return;
            }

            chatRoomMembers.setDeletedAt(null);
            chatRoomMembersRepository.save(chatRoomMembers);
            return;
        }

        // 완전 최초 입장
        ChatRoomMembers firstEnterMember = new ChatRoomMembers();
        firstEnterMember.setUserId(userId);
        firstEnterMember.setChatRoomId(chatRoomId);

        chatRoomMembersRepository.save(firstEnterMember);
    }

    // 채팅방 퇴장
    @Transactional
    public void exitChatRoom(Long userId, Long chatRoomId) {
        ChatRoomMembers chatRoomMembers = chatRoomMembersRepository.findExistMember(userId, chatRoomId)
                .orElseThrow(() -> new RuntimeException("존재 하지 않는 유저 입니다."));

        chatRoomMembers.setDeletedAt(ZonedDateTime.now());
    }



    // 단일 채팅방 상세 조회 ? (참여자 목록 포함)


}
