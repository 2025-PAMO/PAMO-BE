package com.example.demo.dto.music;

import lombok.Data;

@Data
public class CreateMotionMusicRequest {
    private Integer baseMusicId;    // 선택: 기본음악과 연결할 때 사용
    private String inputKey;     // 필수: uploads/{...}.mp4 (프론트 업로드 결과)
    private Double timestampSec; // 선택: null이면 8.0초
}
