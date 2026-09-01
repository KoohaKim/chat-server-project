package com.example.gooha.miniproject.config;

import com.example.grpc.chat.InternalChatServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class GrpcClientConfig {
    @Value("${grpc.server-port}")
    private int selfPort;

    @Value("#{'${grpc.peer-ports}'.split(',')}")
    private List<String> peerPorts;


    @Bean
    public Map<String, InternalChatServiceGrpc.InternalChatServiceBlockingStub> grpcStubs() {
        Map<String, InternalChatServiceGrpc.InternalChatServiceBlockingStub> stubs = new HashMap<>();

        for (String portStr : peerPorts) {
            int port = Integer.parseInt(portStr.trim());
            if (port == selfPort) continue;

            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress("localhost", port)
                    .usePlaintext()
                    .build();

            InternalChatServiceGrpc.InternalChatServiceBlockingStub stub = InternalChatServiceGrpc.newBlockingStub(channel);

            stubs.put(String.valueOf(port), stub);
        }

        return stubs;
    }

}
