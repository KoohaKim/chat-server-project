package com.example.gooha.miniproject.service;

import com.example.gooha.miniproject.dto.chat.ChatRoomMembersResponseDto;
import com.example.gooha.miniproject.dto.chat.ChatRoomResponseDto;
import com.example.gooha.miniproject.entity.main.ChatRoom;
import com.example.gooha.miniproject.entity.main.ChatRoomMembers;
import com.example.gooha.miniproject.redis.ChatRoomMemberCacheService;
import com.example.gooha.miniproject.repository.main.ChatRoomMembersRepository;
import com.example.gooha.miniproject.repository.main.ChatRoomRepository;
import com.example.gooha.miniproject.service.main.ChatRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatRoomServiceTest {

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomMembersRepository chatRoomMembersRepository;

    @MockitoBean
    private ChatRoomMemberCacheService chatRoomMemberCacheService;

    private final Long USER_ID = 1L;
    private final Long CHAT_ROOM_ID = 1L;

    private ChatRoom chatRoom;




    @BeforeEach
    void setUp() {
        chatRoom = new ChatRoom();
        chatRoom.setName("TestChatRoom");
        chatRoom.setGroup(true);
        chatRoom.setRoomOwnerId(1L);
        chatRoomRepository.save(chatRoom);
    }


    @Test
    @DisplayName("조회: 채팅룸 존재 하는지 id로 조회")
    void findChatRoomById() {
        //given
        //when
        ChatRoomResponseDto dto = chatRoomService.findChatRoomById(chatRoom.getId());

        //then
        assertNotNull(dto, "dto가 Null 입니다.");
        assertEquals(dto.getId(), chatRoom.getId(), "id가 일치하지 않습니다.");
        assertEquals(dto.getName(), chatRoom.getName(), "name이 일치하지 않습니다.");
        assertEquals(dto.getIsGroup(), chatRoom.isGroup(), "isGroup이 일치하지 않습니다.");
        assertThrows(RuntimeException.class, () -> {
            chatRoomService.findChatRoomById(9999999L);
        }, "9999999번 채팅방이 존재합니다.");
    }

    @Test
    @DisplayName("조회: 모든 채팅룸 조회")
    void findAllChatRoom() {
        //given
        PageRequest pageRequest = PageRequest.of(0, 10);

        //when
        Page<ChatRoomResponseDto> allChatRoom = chatRoomService.findAllChatRoom(pageRequest);

        //then
        assertNotNull(allChatRoom);
        assertEquals(1, allChatRoom.getTotalPages(), "채팅방 페이지가 일치하지 않습니다.");
        assertEquals("TestChatRoom", allChatRoom.getContent().getFirst().getName(), "채팅방 이름이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("조회: 채팅룸 내 유저들 조회")
    void findChatRoomMembers() {
        //given
        Mockito.when(chatRoomMemberCacheService.getAllUsersInRoom(chatRoom.getId()))
                .thenReturn(Set.of("user1", "user2"));

        //when
        ChatRoomMembersResponseDto chatRoomMembers = chatRoomService.findChatRoomMembers(chatRoom.getId());

        //then
        assertNotNull(chatRoomMembers, "chatRoomMember가 null 입니다.");
        assertEquals(2, chatRoomMembers.getMembers().size(), "chatRoomMembers의 size가 맞지 않습니다.");
        assertTrue(chatRoomMembers.getMembers().contains("user1"), "user1이 존재하지 않습니다.");
        assertTrue(chatRoomMembers.getMembers().contains("user2"), "user2가 존재하지 않습니다.");
    }


    @Test
    @DisplayName("조회: 신규 채팅방 입장 유저 조회")
    void createEntryRecordIfNewUser() {
        //given (DB 비어 있음)

        //when
        chatRoomService.enterChatRoom(USER_ID,CHAT_ROOM_ID);

        //then
        Optional<ChatRoomMembers> saved = chatRoomMembersRepository.findExistMember(USER_ID, CHAT_ROOM_ID);

        assertTrue(saved.isPresent(), "신규 유저 레코드가 생성되지 않았습니다.");
        assertFalse(saved.get().isDeleted(), "신규 생성된 레코드가 deleted 상태입니다.");
        assertEquals(USER_ID, saved.get().getUserId(), "userId가 일치하지 않습니다.");
        assertEquals(CHAT_ROOM_ID, saved.get().getChatRoomId(), "chatRoomId가 일치하지 않습니다.");
    }

}