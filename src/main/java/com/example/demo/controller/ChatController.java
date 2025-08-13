package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.chat.ChatAnswerResponseDTO;
import com.example.demo.dto.chat.ChatRequestDTO;
import com.example.demo.service.ChatService;
import com.example.demo.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "채팅 API", description = "기본음악 생성 시 대화하며 음악 정보를 구체화합니다.")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "GPT 응답 가져오기", description = "질문을 보내면 GPT의 응답과 상태를 반환합니다.")
    @PostMapping("/ask")
    public CustomResponse<ChatAnswerResponseDTO> askQuestion(
            @RequestBody ChatRequestDTO request) {
        Integer userId = SecurityUtil.getCurrentUserId();

        ChatAnswerResponseDTO responseDto = chatService.askGPT(
                userId,
                request.getQuestion()
        );
        return CustomResponse.onSuccess(responseDto);
    }

}
