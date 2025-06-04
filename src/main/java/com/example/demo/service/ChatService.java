package com.example.demo.service;

import com.example.demo.converter.ChatConverter;
import com.example.demo.domain.ChatStatus;
import com.example.demo.domain.Message;
import com.example.demo.domain.User;
import com.example.demo.dto.chat.ChatAnswerResponseDTO;
import com.example.demo.dto.chat.GPTRequestDTO;
import com.example.demo.dto.chat.GPTResponseDTO;
import com.example.demo.repository.UserRepository;
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
    private final UserRepository userRepository;

    // 로컬 채팅 로그 (DB 대신 사용)
    private final Map<Integer, List<Message>> userMessageLog = new HashMap<>();

    public ChatAnswerResponseDTO askGPT(Integer userId, String prompt) {

        ensureUserExists(userId); // 유저 생성 또는 존재 확인

        List<Message> chatLog = userMessageLog.computeIfAbsent(userId, id -> new ArrayList<>());
        chatLog.add(new Message("user", prompt));

        ChatStatus status = determineStatus(prompt);

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
        messages.addAll(chatLog); // 기존 대화 전체 포함

        GPTRequestDTO request = new GPTRequestDTO(model, messages);
        GPTResponseDTO response = template.postForObject(apiURL, request, GPTResponseDTO.class);

        String answer = response.getChoices().get(0).getMessage().getContent();
        chatLog.add(new Message("assistant", answer));

        return ChatConverter.toAnswerDto(answer, status);
    }

    private void ensureUserExists(Integer userId) {
        if (!userRepository.existsById(userId)) {
            User dummy = new User();
            dummy.setNickname("테스트유저");
            dummy.setProfileImage(null);
            dummy.setJoinType("test");
            userRepository.save(dummy);
        }
    }

    private String buildSystemPrompt(ChatStatus status) {
        if (status == ChatStatus.SUMMARY) {
            return "지금까지의 대화를 바탕으로 사용자가 원하는 음악의 특징을 요약해주세요. 다음 형식을 반드시 따르세요: 장르: (예: 락, 팝, 재즈 등), 분위기: (예: 신나는, 잔잔한 등), 장소/상황: (예: 운동할 때, 카페에서 등), 빠르기: (예: 빠름, 중간, 느림), 함께 들을 사람: (예: 친구, 연인 등). 5개 중 최소 3개 이상의 항목을 포함하세요. 대답은 위 형식을 그대로 사용하고, 설명이나 말투를 덧붙이지 마세요.";
        }
        else {
            return
                    "당신은 사용자가 만들고자 하는 음악을 구체화하는 데 도움을 주는 AI입니다. " +
                            "목표는 사용자와의 대화를 통해 음악 생성에 필요한 요소(장르, 분위기, 빠르기, 장소, 함께 들을 사람 등)를 하나씩 수집하는 것입니다. " +
                            "다음 규칙을 따르세요: " +

                            "1. 사용자의 응답이 추상적이면, 그 중 하나의 요소에 대해 구체적인 질문을 던지세요. 예: ‘신나는 음악’ → ‘어떤 장르의 음악을 생각하시나요?’ " +

                            "2. 한 번에 하나의 정보만 물어보세요. 예: 템포, 장소, 분위기 등을 동시에 묻지 마세요. " +

                            "3. 사용자가 '모르겠어요'라고 하면, 예시를 간단히 하나씩 제시하고 선택할 수 있도록 도와주세요. " +

                            "4. 사용자가 '요약해줘'라고 하면, 수집한 정보가 충분한 경우에만 요약을 해주세요. 부족하다면 더 필요한 정보를 알려주세요. " +

                            "5. 사용자가 '그만', '끝', '종료'라고 해도, 요약이 아직 안 됐다면 먼저 요약을 유도하세요. " +

                            "6. 대화가 충분히 구체화되었다고 판단되면, 사용자가 대화를 종료하거나 요약을 요청할 수 있도록 유도하세요. " +
                            "예: '이제 어느 정도 방향이 정해진 것 같아요. 요약해드릴까요?' 또는 '이 정도면 음악을 만들 준비가 된 것 같아요. 마무리해볼까요?' " +

                            "항상 짧고 간결하게, 친절한 어조로 응답하고 대화를 이어가며 필요한 정보를 하나씩 얻어내는 데 집중하세요.";
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
