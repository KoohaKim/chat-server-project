package com.example.gooha.miniproject.controller;

import com.example.gooha.miniproject.dto.message.response.MessageResponseDto;
import com.example.gooha.miniproject.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<Page<MessageResponseDto>> findAllMessagesByChatRoomId(@PathVariable("chatRoomId") Long chatRoomId,
                                                                               @RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MessageResponseDto> allMessage = messageService.findAllMessage(chatRoomId, pageable);
        return ResponseEntity.ok(allMessage);
    }


    @PostMapping("/makeTestMessage")
    public String makeTestMessage(){
        messageService.makeTestMessages();
        return "COMPLETE";
    }

}
