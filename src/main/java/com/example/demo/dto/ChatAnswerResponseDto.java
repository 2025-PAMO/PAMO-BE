package com.example.demo.dto;

import com.example.demo.domain.ChatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatAnswerResponseDto {
    private String message;
    private ChatStatus status;
}
