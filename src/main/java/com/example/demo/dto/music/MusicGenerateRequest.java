package com.example.demo.dto.music;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MusicGenerateRequest {
    private String sessionId;
    private Integer userId;
    private String title;
    private String prompt; // 생성할 음악 프롬프트
}
