package com.example.gooha.miniproject.sharding;

import com.example.gooha.miniproject.dto.message.response.LastMessageResponseDto;
import com.example.gooha.miniproject.repository.message.FindMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageShardExecutor {

    private final Map<String, FindMessageRepository> repositoryMap;

    @Transactional(
            transactionManager = "messageTransactionManager",
            propagation = Propagation.REQUIRES_NEW, // 새 트랜잭션
            readOnly = true
    )
    public List<LastMessageResponseDto> findMessage(List<Long> messageIds) {

        Integer shardKey = ShardContext.getShardKey();

        String beanName = switch (shardKey) {
            case 0 -> "messageShard0Repository";
            case 1 -> "messageShard1Repository";
            default -> throw new IllegalArgumentException("Invalid shardKey: " + shardKey);
        };

        FindMessageRepository repository = repositoryMap.get(beanName);
        return repository.findMessagesByIds(messageIds);
    }
}
