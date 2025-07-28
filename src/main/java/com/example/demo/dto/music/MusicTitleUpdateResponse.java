package com.example.demo.dto.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MusicTitleUpdateResponse {
    private Long musicId;
    private String newTitle;
}