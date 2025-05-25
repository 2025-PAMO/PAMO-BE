package com.example.demo.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 이거 꼭 추가!
public class MusicGenerateRequest {
    private String sessionId;
    private Integer userId;
    private String title;
    private String fileUrl;
}
