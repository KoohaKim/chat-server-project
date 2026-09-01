package com.example.gooha.miniproject.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaChatProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(Long messageId, Long chatRoomId, Long senderId, String content) {
        String topic = "chat-message-topic";
        String partitionKey = String.valueOf(chatRoomId);

        String payload = String.format("%d|%d|%d|%s", messageId, chatRoomId, senderId, content);

        kafkaTemplate.send(topic, partitionKey, payload);
    }

}
