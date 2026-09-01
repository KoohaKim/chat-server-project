package com.example.gooha.miniproject.repository.message;

import com.example.gooha.miniproject.dto.message.response.LastMessageResponseDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageShard1Repository implements FindMessageRepository {
    @PersistenceContext(unitName = "message")
    private EntityManager em;

    @Override
    public List<LastMessageResponseDto> findMessagesByIds(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return List.of();
        }

        return em.createQuery("""
                        SELECT new com.example.gooha.miniproject.dto.message.response.LastMessageResponseDto(
                            m.id, m.content, m.createdAt
                            )
                        FROM Message m
                        WHERE m.id IN :ids
                        """, LastMessageResponseDto.class)
                .setParameter("ids", messageIds)
                .getResultList();
    }
}
