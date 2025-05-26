package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.chat.ChatAnswerResponseDTO;
import com.example.demo.dto.chat.ChatRequestDTO;
import com.example.demo.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "GPT 응답 가져오기", description = "질문을 보내면 GPT의 응답과 상태를 반환합니다.")
    @PostMapping("/ask")
    public CustomResponse<ChatAnswerResponseDTO> askQuestion(
            @RequestBody ChatRequestDTO request,
            @RequestHeader("Authorization") String jwtToken) {

        ChatAnswerResponseDTO responseDto = chatService.askGPT(
                request.getUserId(),
                request.getQuestion(),
                jwtToken
        );
        return CustomResponse.onSuccess(responseDto);
    }

}
