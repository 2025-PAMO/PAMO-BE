package com.example.demo.dto.music;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MotionMusicResponse {
    Integer id;
    Integer baseMusicId;
    String videoS3Key;
    String thumbnailS3Key;
    String thumbnailUrl;
}
