package com.example.demo.service;

import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.User;
import com.example.demo.dto.myPage.BaseMusicDTO;
import com.example.demo.dto.myPage.MotionMusicDTO;
import com.example.demo.dto.myPage.MyMusicResponseDTO;
import com.example.demo.dto.user.UserProfileDTO;
import com.example.demo.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageService {

    private final UserRepository userRepository;
    private final MotionMusicRepository motionMusicRepository;
    private final BaseMusicRepository baseMusicRepository;
    private final MotionMusicLikeRepository motionMusicLikeRepository;
    private final BaseMusicLikeRepository baseMusicLikeRepository;

    @Transactional(readOnly = true)
    public MyMusicResponseDTO getMyMusic(Integer userId, String type) {
        User user = getUserOrThrow(userId);
        UserProfileDTO userProfile = getUserProfile(user);

        return switch (type.toLowerCase()) {
            case "motion" -> buildMyMusicResponse(userProfile, "motion",
                    motionMusicRepository.findByUser(user).stream()
                            .map(m -> toMotionMusicDTO(m, user.getNickname()))
                            .collect(Collectors.toList()));
            case "base" -> buildMyMusicResponse(userProfile, "base",
                    baseMusicRepository.findByUser(user).stream()
                            .map(b -> toBaseMusicDTO(b, userId))
                            .collect(Collectors.toList()));
            default -> throw new IllegalArgumentException("잘못된 type 값입니다. (motion | base)");
        };
    }

    @Transactional(readOnly = true)
    public MyMusicResponseDTO getMyLibrary(Integer userId, String type) {
        User user = getUserOrThrow(userId);
        UserProfileDTO userProfile = getUserProfile(user);

        return switch (type.toLowerCase()) {
            case "motion" -> buildMyMusicResponse(userProfile, "motion",
                    motionMusicLikeRepository.findAllUserLikedVisibleMusicOrderByLikedAtAsc(userId).stream()
                            .map(m -> toMotionMusicDTO(m, m.getUser().getNickname()))
                            .collect(Collectors.toList()));
            case "base" -> buildMyMusicResponse(userProfile, "base",
                    baseMusicLikeRepository.findAllUserLikedBaseMusicOrderByLikedAtAsc(userId).stream()
                            .map(b -> toBaseMusicDTO(b, userId))
                            .collect(Collectors.toList()));
            default -> throw new IllegalArgumentException("잘못된 type 값입니다. (motion | base)");
        };
    }

    private User getUserOrThrow(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private MyMusicResponseDTO buildMyMusicResponse(UserProfileDTO user, String type, List<?> musicList) {
        return MyMusicResponseDTO.builder()
                .user(user)
                .type(type)
                .myMusicList(musicList)
                .build();
    }

    private MotionMusicDTO toMotionMusicDTO(MotionMusic m, String artistName) {
        return MotionMusicDTO.builder()
                .motionMusicId(m.getId())
                .title(m.getTitle())
                .artist(artistName)
                .coverImageUrl(m.getCover())
                .likeCount(m.getLikes() != null ? m.getLikes().size() : 0)
                .visibility(Boolean.TRUE.equals(m.getVisibility()))
                .build();
    }

    private BaseMusicDTO toBaseMusicDTO(BaseMusic b, Integer viewerUserId) {
        return BaseMusicDTO.builder()
                .baseMusicId(b.getId())
                .title(b.getTitle())
                .artist(b.getUser().getNickname())
                .musicFileUrl(b.getFileUrl())
                .isBookmarked(b.getLikes().stream().anyMatch(like -> like.getUser().getId().equals(viewerUserId)))
                .build();
    }

    public UserProfileDTO getUserProfile(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .providerType(user.getProviderType())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .build();
    }

    @Transactional
    public void updateVisibility(Integer musicId, Boolean visibility) {
        MotionMusic motionMusic = motionMusicRepository.findById(musicId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모션 음악을 찾을 수 없습니다."));

        motionMusic.setVisibility(visibility);
    }

    @Transactional
    public void updateMotionMusicTitle(Integer id, String newTitle) {
        MotionMusic motionMusic = motionMusicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 모션 음악을 찾을 수 없습니다."));

        motionMusic.setTitle(newTitle);
    }
}
