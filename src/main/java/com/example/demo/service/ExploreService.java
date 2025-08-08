package com.example.demo.service;

import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.User;
import com.example.demo.dto.explore.ExploreResponseDTO;
import com.example.demo.repository.MotionMusicLikeRepository;
import com.example.demo.repository.MotionMusicRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.converter.ExploreConverter.convertToTitleAndArtistList;

@RequiredArgsConstructor
@Service
public class ExploreService {
    private final UserRepository userRepository;
    private final MotionMusicRepository motionMusicRepository;
    private final MotionMusicLikeRepository motionMusicLikeRepository;

    public ExploreResponseDTO getExplorePage(Integer id) {
        return ExploreResponseDTO.builder()
                .profileImageUrl(getProfileImageUrl(id))
                .mostStreamed(convertToTitleAndArtistList(getMostStreamedMotionMusic()))
                .mostLoved(convertToTitleAndArtistList(getMostLovedMotionMusic()))
                .newReleases(convertToTitleAndArtistList(getLatestMotionMusic()))
                .favorites(convertToTitleAndArtistList(getUserRecentlyLikedMusics(id)))
                .build();
    }

    public ExploreResponseDTO getExplorePage() {
        return ExploreResponseDTO.builder()
                .mostStreamed(convertToTitleAndArtistList(getMostStreamedMotionMusic()))
                .mostLoved(convertToTitleAndArtistList(getMostLovedMotionMusic()))
                .newReleases(convertToTitleAndArtistList(getLatestMotionMusic()))
                .favorites(null)
                .build();
    }

    private String getProfileImageUrl(Integer userId) {
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getProfileImage();
    }

    private List<MotionMusic> getMostStreamedMotionMusic() {
        return motionMusicRepository.findByVisibilityTrueOrderByCountDesc(PageRequest.of(0, 30));
    }

    private List<MotionMusic> getMostLovedMotionMusic() {
        return motionMusicRepository.findMostLikedVisibleMotionMusic(PageRequest.of(0, 50));
    }

    private List<MotionMusic> getLatestMotionMusic() {
        return motionMusicRepository.findByVisibilityTrueOrderByCreatedAtDesc(PageRequest.of(0, 4));
    }

    // 사용자가 좋아요한 시점 기준 최신 3개
    private List<MotionMusic> getUserRecentlyLikedMusics(Integer userId) {
        return motionMusicLikeRepository.findUserLikedVisibleMusicOrderByLikedAtDesc(
                userId,
                PageRequest.of(0, 3)
        );
    }




}