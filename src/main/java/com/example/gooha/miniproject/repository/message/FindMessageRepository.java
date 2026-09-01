package com.example.gooha.miniproject.repository.message;


import com.example.gooha.miniproject.dto.message.response.LastMessageResponseDto;

import java.util.List;


public interface FindMessageRepository {
    List<LastMessageResponseDto> findMessagesByIds(List<Long> messageIds);
}
