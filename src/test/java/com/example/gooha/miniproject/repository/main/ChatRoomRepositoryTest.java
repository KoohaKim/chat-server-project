package com.example.gooha.miniproject.repository.main;

import com.example.gooha.miniproject.entity.main.ChatRoom;
import com.example.gooha.miniproject.entity.main.ChatRoomMembers;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ChatRoomRepositoryTest {

    @Autowired
    ChatRoomRepository chatRoomRepository;


    @Autowired
    private EntityManager em;

    @Test
    @Transactional
    @DisplayName("chatRoomId로 userId 리스트 조회")
    void findUserIdsByChatroomId(){
        //given
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName("testRoom");
        chatRoom.setGroup(false);
        em.persist(chatRoom);

        ChatRoomMembers member1 = new ChatRoomMembers();
        member1.setChatRoomId(chatRoom.getId());
        member1.setUserId(998L);
        em.persist(member1);

        ChatRoomMembers member2 = new ChatRoomMembers();
        member2.setChatRoomId(chatRoom.getId());
        member2.setUserId(999L);
        em.persist(member2);

        em.flush();
        em.clear();

        //when
        List<Long> userIdsByChatroomId = chatRoomRepository.findUserIdsByChatroomId(chatRoom.getId());

        //then
        assertEquals(userIdsByChatroomId.get(0), member1.getUserId(),"member1 id가 없음");
        assertEquals(userIdsByChatroomId.get(1), member2.getUserId(),"member2 id가 없음");
    }
}