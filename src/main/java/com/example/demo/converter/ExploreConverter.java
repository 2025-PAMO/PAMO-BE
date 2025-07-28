package com.example.demo.converter;

import com.example.demo.domain.MotionMusic;
import com.example.demo.dto.explore.MotionMusicTitleAndArtist;

import java.util.List;
import java.util.stream.Collectors;

public class ExploreConverter {
    public static MotionMusicTitleAndArtist convertMotionMusicTitleAndArtist(MotionMusic motionMusic){
        return MotionMusicTitleAndArtist.builder()
                .motionMusicId(motionMusic.getId())
                .title(motionMusic.getTitle())
                .artistProfileImage(motionMusic.getUser().getProfileImage())
                .artist(motionMusic.getUser().getNickname())
                .coverImageUrl(motionMusic.getCover())
                .build();
    }

    public static List<MotionMusicTitleAndArtist> convertToTitleAndArtistList(List<MotionMusic> motionMusicList) {
        return motionMusicList.stream()
                .map(ExploreConverter::convertMotionMusicTitleAndArtist)
                .collect(Collectors.toList());
    }
}
