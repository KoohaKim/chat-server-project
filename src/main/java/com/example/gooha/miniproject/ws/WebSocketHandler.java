package com.example.gooha.miniproject.ws;

import com.example.gooha.miniproject.redis.ChatRoomMemberCacheService;
import com.example.gooha.miniproject.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {
    private final MessageService messageService;
    private final SessionManager sessionManager;
    private final ChatRoomMemberCacheService chatRoomMemberCacheService;


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        Long userId = (Long) session.getAttributes().get("userId");
        Long chatRoomId = (Long) session.getAttributes().get("chatRoomId");

        messageService.sendMessage(chatRoomId, userId, payload);

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long chatRoomId = (Long) session.getAttributes().get("chatRoomId");
        Long userId = (Long) session.getAttributes().get("userId");

        sessionManager.registerSession(chatRoomId, session);
        log.info("연결된 세션: chatRoomId={}, 세션 ID={}, 유저 Id = {}", chatRoomId, session.getId(), userId);

        chatRoomMemberCacheService.addUserToRoom(chatRoomId, userId.toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long chatRoomId = (Long) session.getAttributes().get("chatRoomId");
        Long userId = (Long) session.getAttributes().get("userId");

        sessionManager.removeSession(chatRoomId,session);
        chatRoomMemberCacheService.removeUserFromRoom(chatRoomId, userId.toString());

    }



}
