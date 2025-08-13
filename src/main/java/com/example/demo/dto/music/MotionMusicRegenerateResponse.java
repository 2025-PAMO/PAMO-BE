package com.example.demo.dto.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MotionMusicRegenerateResponse {
    private Integer motionMusicId;
    private String fileUrl;
    private String baseTitle;
    private final String motionTitle;
}
