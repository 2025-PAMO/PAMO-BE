package com.example.demo.dto.explore;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExploreResponseDTO {
    List<MotionMusicTitleAndArtist> mostStreamed;
    List<MotionMusicTitleAndArtist> mostLoved;
    List<MotionMusicTitleAndArtist> newReleases;
    List<MotionMusicTitleAndArtist> favorites;
}
