package com.example.demo.dto.explore;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

// Explore 테스트용 DTO
@Getter
@AllArgsConstructor
public class MusicDebugDTO {
    private Integer id;
    private String title;
    private String artist;
    private String coverImageUrl;
    private Timestamp createdAt;
}
