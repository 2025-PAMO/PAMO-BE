package com.example.demo.service;

import com.example.demo.converter.ChatConverter;
import com.example.demo.domain.ChatStatus;
import com.example.demo.dto.ChatAnswerResponseDto;
import com.example.demo.dto.GPTRequestDto;
import com.example.demo.dto.GPTResponseDto;
import com.example.demo.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.chat-url}")
    private String apiURL;

    private final RestTemplate template;

    // 로컬 채팅 로그 (DB 대신 사용)
    private final Map<Integer, List<Message>> userMessageLog = new HashMap<>();

    public ChatAnswerResponseDto askGPT(Integer userId, String prompt, String jwtToken) {
        // 1. 유저 메시지 로그 초기화 또는 조회
        List<Message> chatLog = userMessageLog.computeIfAbsent(userId, id -> new ArrayList<>());

        // 2. 사용자 메시지 추가
        chatLog.add(new Message("user", prompt));

        // 3. 상태 판단 (요약/정리/끝/기타)
        ChatStatus status = determineStatus(prompt);

        String processedPrompt;

        switch (status) {
            case SUMMARY -> {
                if (isContentSufficient(chatLog)) {
                    processedPrompt = buildSummaryPrompt(chatLog);
                } else {
                    return ChatConverter.toAnswerDto("요약을 위해 더 많은 정보가 필요해요. 이어서 대화를 진행해주세요.", ChatStatus.CHAT);
                }
            }
            case END -> {
                if (!hasSummarized(chatLog)) {
                    return ChatConverter.toAnswerDto("먼저 내용을 요약한 뒤에 종료할 수 있어요. '요약해줘'라고 요청해보세요.", ChatStatus.CHAT);
                }
                String finalSummary = extractSummary(chatLog);
                // TODO: finalSummary를 DB에 저장하는 로직
                userMessageLog.remove(userId); // 종료되었으니 로컬 기록 제거
                return ChatConverter.toAnswerDto("대화를 종료합니다. 요약 내용:\n" + finalSummary, ChatStatus.END);
            }
            case CHAT -> {
                processedPrompt = prompt; // 일반 대화
            }
            default -> processedPrompt = prompt;
        }

        // 4. GPT 요청
        GPTRequestDto request = new GPTRequestDto(model, processedPrompt);
        GPTResponseDto response = template.postForObject(apiURL, request, GPTResponseDto.class);

        String answer = response.getChoices().get(0).getMessage().getContent();

        // 5. 답변 저장
        chatLog.add(new Message("assistant", answer));

        // 6. 응답 반환
        return ChatConverter.toAnswerDto(answer, status);
    }

    private ChatStatus determineStatus(String prompt) {
        String lower = prompt.toLowerCase();
        if (lower.contains("요약") || lower.contains("정리")) return ChatStatus.SUMMARY;
        if (lower.contains("끝") || lower.contains("종료") || lower.contains("그만")) return ChatStatus.END;
        return ChatStatus.CHAT;
    }

    private boolean isContentSufficient(List<Message> log) {
        // 예: user 메시지 3개 이상이면 충분하다고 판단
        return log.stream().filter(m -> m.getRole().equals("user")).count() >= 3;
    }

    private boolean hasSummarized(List<Message> log) {
        // 예: assistant가 "요약" 관련 응답을 한 적이 있는지 확인
        return log.stream().anyMatch(m ->
                m.getRole().equals("assistant") && m.getContent().toLowerCase().contains("요약"));
    }

    private String buildSummaryPrompt(List<Message> log) {
        StringBuilder sb = new StringBuilder("다음 대화를 요약해줘:\n");
        for (Message m : log) {
            sb.append(m.getRole()).append(": ").append(m.getContent()).append("\n");
        }
        return sb.toString();
    }

    private String extractSummary(List<Message> log) {
        // 마지막 assistant 메시지를 요약이라고 간주
        return log.stream()
                .filter(m -> m.getRole().equals("assistant") && m.getContent().toLowerCase().contains("요약"))
                .map(Message::getContent)
                .reduce((first, second) -> second)
                .orElse("요약 없음");
    }
}
