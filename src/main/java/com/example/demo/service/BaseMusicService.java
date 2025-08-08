package com.example.demo.service;

import com.example.demo.apiPayload.exception.CustomException;
import com.example.demo.apiPayload.code.MusicErrorCode;
import com.example.demo.domain.BaseMusic;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaseMusicService {
    private final BaseMusicRepository baseMusicRepository;

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

    private BaseMusic getBaseMusicOrThrow(Integer baseMusicId) {
        return baseMusicRepository.findById(baseMusicId)
                .orElseThrow(() -> new CustomException(MusicErrorCode.MUSIC_NOT_FOUND));
    }

    private void validateOwnership(Integer ownerId, Integer currentUserId) {
        if (!ownerId.equals(currentUserId)) {
            throw new CustomException(MusicErrorCode.NO_PERMISSION);
        }
    }
    
}
