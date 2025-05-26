package com.example.demo.dto.chat;

import com.example.demo.domain.ChatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatAnswerResponseDTO {
    private String message;
    private ChatStatus status;
}
