package com.example.demo.converter;

import com.example.demo.dto.chat.ChatAnswerResponseDTO;
import com.example.demo.domain.ChatStatus;

public class ChatConverter {
    public static ChatAnswerResponseDTO toAnswerDto(String message, ChatStatus status, String sessionId) {
        return ChatAnswerResponseDTO.builder()
                .message(message)
                .status(status)
                .sessionId(sessionId)
                .build();
    }
}
