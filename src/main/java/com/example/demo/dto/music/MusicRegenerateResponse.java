package com.example.demo.dto.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MusicRegenerateResponse {
    private Integer musicId;
    private String fileUrl;
    private String title;
}
