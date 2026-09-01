package com.example.gooha.miniproject.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionManager {
    private final Map<Long, Set<WebSocketSession>> chatRoomSessions = new ConcurrentHashMap<>();

    public void registerSession(Long chatRoomId, WebSocketSession session) {
        chatRoomSessions.computeIfAbsent(chatRoomId, k -> new HashSet<>()).add(session);
        log.info("세션 등록 완료: chatRoomId={}, 세션 ID={}", chatRoomId, session.getId());

    }

    public void removeSession(Long chatRoomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = chatRoomSessions.get(chatRoomId);

        if(session != null) {
            sessions.remove(session);
            log.info("세션 제거 완료: chatRoomId={}, 세션 ID={}", chatRoomId, session.getId());
        }
    }

    public void broadcast(Long chatRoomId, String message) {
        Set<WebSocketSession> sessions = chatRoomSessions.get(chatRoomId);

        if(sessions != null){
            for(WebSocketSession session : sessions){
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("메시지 전송 실패: 세션 ID={}", session.getId());
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Set<WebSocketSession> getSession(Long chatRoomId) {
        return chatRoomSessions.get(chatRoomId);
    }

}
