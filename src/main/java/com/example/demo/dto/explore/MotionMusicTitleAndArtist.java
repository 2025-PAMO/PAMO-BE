package com.example.demo.dto.explore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MotionMusicTitleAndArtist {
    private Integer motionMusicId;
    private String title;
    private String artistProfileImage;
    private String artist;
    private String coverImageUrl;
}
