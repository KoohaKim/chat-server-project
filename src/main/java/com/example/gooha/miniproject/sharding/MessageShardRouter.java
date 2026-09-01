package com.example.gooha.miniproject.sharding;

import com.example.gooha.miniproject.dto.message.response.LastMessageResponseDto;
import com.example.gooha.miniproject.repository.message.FindMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageShardRouter {
    private final MessageShardExecutor executor;

    public List<LastMessageResponseDto> findMessage(Integer shardKey, List<Long> messageIds) {
        ShardContext.setShardKey(shardKey);

        try {
            return executor.findMessage(messageIds);
        } finally {
            ShardContext.clear(); // 스레드 로컬에 남아있는 샤드키 제거
        }
    }

}
