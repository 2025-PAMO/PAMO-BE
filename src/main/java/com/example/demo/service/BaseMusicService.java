package com.example.demo.service;

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
        BaseMusic baseMusic = baseMusicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 기본 음악을 찾을 수 없습니다."));

        if (!baseMusic.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 기본 음악에 대한 수정 권한이 없습니다.");
        }

        baseMusic.setTitle(newTitle);
    }

}
