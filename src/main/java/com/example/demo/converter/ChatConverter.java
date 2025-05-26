package com.example.demo.converter;

import com.example.demo.dto.ChatAnswerResponseDto;
import com.example.demo.domain.ChatStatus;

public class ChatConverter {
    public static ChatAnswerResponseDto toAnswerDto(String message, ChatStatus status) {
        return ChatAnswerResponseDto.builder()
                .message(message)
                .status(status)
                .build();
    }
}
