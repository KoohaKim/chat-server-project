package com.example.gooha.miniproject.config;

import com.example.gooha.miniproject.ws.InternalChatServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcServerConfig {
    @Value("${grpc.server-port}")
    private int grpcPort;
    private Server server;
    private final InternalChatServiceImpl internalChatService;

    @PostConstruct
    public void startServer() {
        try {
            server = ServerBuilder
                    .forPort(grpcPort)
                    .addService(internalChatService)
                    .build()
                    .start();

            log.info("gRPC 서버가 포트 {}에서 시작되었습니다.", grpcPort);

            // 서버 종료 시 안전하게 종료하도록 ?
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("gRPC 서버 종료 중...");
                server.shutdown();
            }));
        } catch (Exception e) {
            log.error("gRPC 서버 시작 실패: {}", e.getMessage());
        }
    }
}
