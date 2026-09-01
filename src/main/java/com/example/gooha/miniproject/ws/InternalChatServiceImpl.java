package com.example.gooha.miniproject.ws;

import com.example.grpc.chat.InternalChatServiceGrpc;
import com.example.grpc.chat.RelayRequest;
import com.example.grpc.chat.RelayResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InternalChatServiceImpl extends InternalChatServiceGrpc.InternalChatServiceImplBase {
    private final SessionManager sessionManager;

    @Override
    public void relayMessage(RelayRequest request, StreamObserver<RelayResponse> responseObserver) {
        try {
            Long chatRoomId = Long.parseLong(request.getRoomId());
            Long senderId = Long.parseLong(request.getSenderId());
            String content = request.getMessage();

            log.info("gRPC 메시지 수신: chatRoomId={}, senderId={}, message={}", chatRoomId, senderId, content);

            String formattedMessage = String.format("[Room %d] User %s: %s", chatRoomId, senderId, content);
            sessionManager.broadcast(chatRoomId, formattedMessage);

            RelayResponse response = RelayResponse.newBuilder()
                    .setDelivered(true)
                    .setMessage("BroadCast 완료")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC 메시지 수신 처리 실패", e);
            RelayResponse response = RelayResponse.newBuilder()
                    .setDelivered(false)
                    .setMessage("에러 발생: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }


}
