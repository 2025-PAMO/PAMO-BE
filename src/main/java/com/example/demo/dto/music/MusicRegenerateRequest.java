package com.example.demo.dto.music;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MusicRegenerateRequest {
    private String sessionId;
    private Integer userId;
    private String title;
}
