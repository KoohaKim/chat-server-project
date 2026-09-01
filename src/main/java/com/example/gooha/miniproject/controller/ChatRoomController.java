package com.example.gooha.miniproject.controller;

import com.example.gooha.miniproject.dto.chat.ChatRoomCreateRequestDto;
import com.example.gooha.miniproject.dto.chat.ChatRoomCreateResponseDto;
import com.example.gooha.miniproject.dto.chat.ChatRoomMembersResponseDto;
import com.example.gooha.miniproject.dto.chat.ChatRoomResponseDto;
import com.example.gooha.miniproject.dto.chat.ChatRoomListResponseDto;
import com.example.gooha.miniproject.entity.main.ChatRoom;
import com.example.gooha.miniproject.jwt.JwtProvider;
import com.example.gooha.miniproject.service.main.ChatRoomQueryService;
import com.example.gooha.miniproject.service.main.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatRooms")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final JwtProvider jwtProvider;
    private final ChatRoomQueryService chatRoomQueryService;

    @PostMapping("/createChatRoom")
    public ResponseEntity<ChatRoomCreateResponseDto> createChatRoom(@RequestBody ChatRoomCreateRequestDto requestDto) {

        ChatRoom chatRoom = chatRoomService.createChatRoom(requestDto.getRoomName(), requestDto.getRoomOwnerId());

        return ResponseEntity.ok(new ChatRoomCreateResponseDto(chatRoom.getId(), chatRoom.getName(), chatRoom.getRoomOwnerId()));
    }


    @GetMapping("/allChatRoom")
    public ResponseEntity<Page<ChatRoomResponseDto>> findAllChatRooms(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatRoomResponseDto> allChatRoom = chatRoomService.findAllChatRoom(pageable);
        return ResponseEntity.ok(allChatRoom);
    }

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoomResponseDto> findChatRoomById(@PathVariable("chatRoomId") Long chatRoomId) {
        ChatRoomResponseDto chatRoom = chatRoomService.findChatRoomById(chatRoomId);
        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping("/{chatRoomId}/members")
    public ResponseEntity<ChatRoomMembersResponseDto> findChatRoomMembers(@PathVariable("chatRoomId") Long chatRoomId){
        ChatRoomMembersResponseDto chatRoomMembers = chatRoomService.findChatRoomMembers(chatRoomId);
        return ResponseEntity.ok(chatRoomMembers);
    }



    @PostMapping("/MyChatRooms/{chatRoomId}/enter")
    public ResponseEntity<String> enterChatRoom(
            @RequestHeader("Authorization") String token,
            @PathVariable("chatRoomId")Long chatRoomId) {

        Long userId = jwtProvider.getUserIdByToken(token);

        chatRoomService.enterChatRoom(userId, chatRoomId);
        return ResponseEntity.ok("entered");
    }

    @PostMapping("/MyChatRooms/{chatRoomId}/exit")
    public ResponseEntity<String> exitRoom(
            @RequestHeader("Authorization") String token,
            @PathVariable("chatRoomId") Long chatRoomId) {

        Long userId = jwtProvider.getUserIdByToken(token);
        chatRoomService.exitChatRoom(userId, chatRoomId);
        return ResponseEntity.ok("exit");
    }


    @GetMapping("/MyChatRooms/{userId}")
    public ResponseEntity<List<ChatRoomListResponseDto>> getMyChatRooms(@PathVariable("userId")Long userId){
        List<ChatRoomListResponseDto> myChatRooms = chatRoomQueryService.getMyChatRooms(userId);

        return ResponseEntity.ok(myChatRooms);
    }


}
