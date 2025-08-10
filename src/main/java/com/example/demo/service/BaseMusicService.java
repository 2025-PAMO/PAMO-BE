package com.example.demo.service;

import com.example.demo.apiPayload.code.GeneralErrorCode;
import com.example.demo.apiPayload.exception.CustomException;
import com.example.demo.apiPayload.code.MusicErrorCode;
import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.BaseMusicLike;
import com.example.demo.domain.User;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaseMusicService {
    private final BaseMusicRepository baseMusicRepository;
    private final BaseMusicLikeRepository baseMusicLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateBaseMusicTitle(Integer userId, Integer id, String newTitle) {
        BaseMusic baseMusic = getBaseMusicOrThrow(id);

        validateOwnership(baseMusic.getUser().getId(), userId);

        baseMusic.setTitle(newTitle);
    }

    @Transactional
    public void deleteBaseMusic( Integer userId, Integer musicId) {
        BaseMusic baseMusic = getBaseMusicOrThrow(musicId);

        // 소유자 확인
        validateOwnership(baseMusic.getUser().getId(), userId);

        // BaseMusic의 deletable 값이 false라면 삭제 불가
        if (!Boolean.TRUE.equals(baseMusic.getDeletable())) {
            throw new CustomException(MusicErrorCode.BASE_MUSIC_NOT_DELETABLE);
        }

        // 실제 삭제
        baseMusicRepository.delete(baseMusic);
    }

    @Transactional
    public void bookmark(Integer userId, Integer baseMusicId) {
        BaseMusic baseMusic = getBaseMusicOrThrow(baseMusicId);
        User user = getUserOrThrow(userId);

        if (baseMusicLikeRepository.existsByUserIdAndBaseMusicId(userId, baseMusicId)) return;

        try {
            BaseMusicLike like = new BaseMusicLike();
            like.setUser(user);
            like.setBaseMusic(baseMusic);
            baseMusicLikeRepository.save(like);
        } catch (DataIntegrityViolationException ignored) {
        }
    }

    @Transactional
    public void unbookmark(Integer userId, Integer baseMusicId) {
        getBaseMusicOrThrow(baseMusicId);

        baseMusicLikeRepository.deleteByUserIdAndBaseMusicId(userId, baseMusicId);
    }


    private BaseMusic getBaseMusicOrThrow(Integer baseMusicId) {
        return baseMusicRepository.findById(baseMusicId)
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
