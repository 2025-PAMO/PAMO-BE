package com.example.demo.service;

import com.example.demo.converter.ChatConverter;
import com.example.demo.domain.ChatStatus;
import com.example.demo.domain.Message;
import com.example.demo.dto.ChatAnswerResponseDto;
import com.example.demo.dto.GPTRequestDto;
import com.example.demo.dto.GPTResponseDto;
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
        List<Message> chatLog = userMessageLog.computeIfAbsent(userId, id -> new ArrayList<>());
        chatLog.add(new Message("user", prompt));

        ChatStatus status = determineStatus(prompt);
        String processedPrompt = prompt;

        if (status == ChatStatus.SUMMARY) {
            if (!isContentSufficient(chatLog)) {
                return ChatConverter.toAnswerDto("요약을 위해 더 많은 정보가 필요해요. 이어서 대화를 진행해주세요.", ChatStatus.CHAT);
            }
        } else if (status == ChatStatus.END) {
            if (!hasSummarized(chatLog)) {
                return ChatConverter.toAnswerDto("먼저 내용을 요약한 뒤에 종료할 수 있어요. '요약해줘'라고 요청해보세요.", ChatStatus.CHAT);
            }
            String finalSummary = extractSummary(chatLog);
            userMessageLog.remove(userId);
            return ChatConverter.toAnswerDto("대화를 종료합니다. 요약 내용:\n" + finalSummary, ChatStatus.END);
        }

        // GPT 요청 메시지 구성
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", buildSystemPrompt(status)));

        if (status == ChatStatus.SUMMARY) {
            messages.addAll(chatLog);
        } else {
            messages.add(new Message("user", processedPrompt));
        }

        GPTRequestDto request = new GPTRequestDto(model, messages);
        GPTResponseDto response = template.postForObject(apiURL, request, GPTResponseDto.class);

        String answer = response.getChoices().get(0).getMessage().getContent();
        chatLog.add(new Message("assistant", answer));

        return ChatConverter.toAnswerDto(answer, status);
    }

    private String buildSystemPrompt(ChatStatus status) {
        if (status == ChatStatus.SUMMARY) {
            return "다음 정보를 바탕으로 사용자 의도를 요약해주세요. [장르: ~, 분위기: ~, 장소: ~, 빠르기: ~, 함께 들을 사람: ~]";
        } else {
            return "당신은 사용자가 만들고자 하는 음악을 구체화하는 데 도움을 주는 AI입니다.\n"
                    + "당신의 목적은 사용자와의 대화를 통해 음악 생성에 필요한 요소(장르, 분위기, 빠르기, 사용될 상황, 함께 듣고 싶은 사람 등)를 자세히 수집하는 것입니다.\n"
                    + "\n"
                    + "아래 조건을 반드시 따르세요:\n"
                    + "\n"
                    + "1. 대화의 목적은 음악 생성에 필요한 정보를 수집하는 것입니다. 음악과 무관한 질문에는 “저는 음악 생성 전용 AI입니다. 음악 생성에 대한 질문만 도와드릴 수 있어요.”라고 응답하십시오.\n"
                    + "\n"
                    + "2. 사용자가 추상적으로 대답할 경우(예: “신나는 음악”) 구체적인 질문을 이어가세요. 예: “그 신나는 음악은 어떤 장르를 생각하시나요?”, “이 음악은 어떤 장소에서 듣고 싶으신가요?” 등.\n"
                    + "\n"
                    + "3. 사용자가 \"모르겠어요\"라고 답하면, 예시나 제안을 제공해 선택지를 넓혀주세요. 예: “그렇다면 이런 장르는 어떠세요? 일렉트로닉, 팝, 락 등…”\n"
                    + "\n"
                    + "4. 사용자가 \"요약해줘\"라고 하면 지금까지의 대화 내용이 충분한 경우에만 요약을 제공합니다.\n"
                    + "   - 충분함의 기준은 장르/빠르기/장소/상황/함께 듣고 싶은 사람 등 **5개 요소 중 3개 이상이 포함되어야 합니다.**\n"
                    + "   - 불충분한 경우에는 “아직 요약하기엔 정보가 부족해요. 몇 가지 더 여쭤볼게요.”라고 답하고 대화를 이어가세요.\n"
                    + "\n"
                    + "5. 사용자가 \"그만\", \"끝\", \"종료\"와 같은 종료 요청을 할 경우에도, 요약이 먼저 이루어지지 않았다면 종료하지 말고 “요약이 먼저 필요해요. ‘요약해줘’라고 요청해주세요.”라고 안내하십시오.\n"
                    + "\n"
                    + "항상 친절하고 대화를 이어가며, 사용자에게 질문을 던지고 음악적 상상력을 넓혀주는 역할을 하십시오.";
        }
    }

    private ChatStatus determineStatus(String prompt) {
        String lower = prompt.toLowerCase();
        if (lower.contains("요약") || lower.contains("정리")) return ChatStatus.SUMMARY;
        if (lower.contains("끝") || lower.contains("종료") || lower.contains("그만")) return ChatStatus.END;
        return ChatStatus.CHAT;
    }

    private boolean isContentSufficient(List<Message> log) {
        return log.stream().filter(m -> m.getRole().equals("user")).count() >= 3;
    }

    private boolean hasSummarized(List<Message> log) {
        return log.stream().anyMatch(m ->
                m.getRole().equals("assistant") && m.getContent().toLowerCase().contains("요약"));
    }

    private String extractSummary(List<Message> log) {
        return log.stream()
                .filter(m -> m.getRole().equals("assistant") && m.getContent().toLowerCase().contains("요약"))
                .map(Message::getContent)
                .reduce((first, second) -> second)
                .orElse("요약 없음");
    }
}
