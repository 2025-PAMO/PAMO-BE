package com.example.demo.service;

import com.example.demo.apiPayload.code.GeneralErrorCode;
import com.example.demo.apiPayload.code.MusicErrorCode;
import com.example.demo.apiPayload.exception.CustomException;
import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.MotionMusicLike;
import com.example.demo.domain.User;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MotionMusicService {
    private final MotionMusicRepository motionMusicRepository;
    private final MotionMusicLikeRepository motionMusicLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateVisibility(Integer userId, Integer musicId, Boolean visibility) {
        MotionMusic motionMusic = getMotionMusicOrThrow(musicId);

        validateOwnership(motionMusic.getUser().getId(), userId);

        motionMusic.setVisibility(visibility);
    }

    @Transactional
    public void updateMotionMusicTitle(Integer userId, Integer id, String newTitle) {
        MotionMusic motionMusic = getMotionMusicOrThrow(id);

        validateOwnership(motionMusic.getUser().getId(), userId);

        motionMusic.setTitle(newTitle);
    }

    @Transactional
    public void deleteMotionMusic(Integer userId, Integer musicId) {
        MotionMusic motionMusic = getMotionMusicOrThrow(musicId);

        validateOwnership(motionMusic.getUser().getId(), userId);

        BaseMusic baseMusic = motionMusic.getBaseMusic();

        motionMusicRepository.delete(motionMusic);

        // 연결된 BaseMusic의 다른 모션 음악이 있는지 확인
        List<MotionMusic> remaining = motionMusicRepository.findByBaseMusicId(baseMusic.getId());
        if (remaining.isEmpty()) baseMusic.setDeletable(true);

    }

    @Transactional
    public void like(Integer userId, Integer motionMusicId) {
        MotionMusic music = getMotionMusicOrThrow(motionMusicId);
        User user = getUserOrThrow(userId);

        // 비공개곡: 본인만 좋아요 가능
         if (Boolean.FALSE.equals(music.getVisibility())) {
             validateOwnership(music.getUser().getId(), userId);
         }

        // 이미 좋아요면 종료 (멱등)
        if (motionMusicLikeRepository.existsByUserIdAndMotionMusicId(userId, motionMusicId)) return;

        try {
            MotionMusicLike like = new MotionMusicLike();
            like.setUser(user);
            like.setMotionMusic(music);
            motionMusicLikeRepository.save(like);
        } catch (DataIntegrityViolationException ignored) {
        }
    }


    private MotionMusic getMotionMusicOrThrow(Integer motionMusicId) {
        return motionMusicRepository.findById(motionMusicId)
                .orElseThrow(() -> new CustomException(MusicErrorCode.MUSIC_NOT_FOUND));
    }

    private void validateOwnership(Integer ownerId, Integer currentUserId) {
        if (!ownerId.equals(currentUserId)) {
            throw new CustomException(MusicErrorCode.NO_PERMISSION);
        }
    }

    private User getUserOrThrow(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GeneralErrorCode.USER_NOT_FOUND));
    }

}
