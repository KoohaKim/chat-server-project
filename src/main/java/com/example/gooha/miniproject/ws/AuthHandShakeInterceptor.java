package com.example.gooha.miniproject.ws;

import com.example.gooha.miniproject.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class AuthHandShakeInterceptor implements HandshakeInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        System.out.println("[Handshake] 요청 들어옴: URI = " + request.getURI());

        try{
            Map<String, String> queryParams = parseQueryParams(request.getURI().getQuery());
            String token = queryParams.get("token");
            String chatRoomIdStr = queryParams.get("chatRoomId");

            if(token == null || chatRoomIdStr == null || !jwtProvider.validateToken(token)){
                System.out.println("Handshake 실패: 토큰 유효성 실패 또는 파라미터 부족");
                return false;
            }

            Long userId = jwtProvider.getUserIdByToken(token);
            Long chatRoomId = Long.valueOf(chatRoomIdStr);
            System.out.println("받은 token = " + token);
            System.out.println("받은 chatRoomId = " + chatRoomIdStr);


            attributes.put("chatRoomId", chatRoomId);
            attributes.put("userId", userId);
            System.out.println("Handshake 성공: userId=" + userId + ", chatRoomId=" + chatRoomId);

            return true;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Handshake 실패: 예외 발생");

            return false;
        }

    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }


    private Map<String, String> parseQueryParams(String query) {
        return Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
    }

}
