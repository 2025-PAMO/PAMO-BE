package com.example.demo.service;

import com.example.demo.domain.MotionMusic;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MotionMusicService {
    private final MotionMusicRepository motionMusicRepository;

    @Transactional
    public void updateVisibility(Integer userId, Integer musicId, Boolean visibility) {
        MotionMusic motionMusic = motionMusicRepository.findById(musicId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모션 음악을 찾을 수 없습니다."));

        if (!motionMusic.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 모션 음악에 대한 수정 권한이 없습니다.");
        }

        motionMusic.setVisibility(visibility);
    }

    @Transactional
    public void updateMotionMusicTitle(Integer userId, Integer id, String newTitle) {
        MotionMusic motionMusic = motionMusicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 모션 음악을 찾을 수 없습니다."));

        if (!motionMusic.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 모션 음악에 대한 수정 권한이 없습니다.");
        }

        motionMusic.setTitle(newTitle);
    }

}
