package com.example.demo.dto.myPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LibraryBaseMusicDTO {
    private Integer baseMusicId;
    private String title;
    private String artist;
    private String musicFileUrl;
    private boolean isBookmarked;
}
