package com.example.demo.repository;

import com.example.demo.domain.MotionMusic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MotionMusicRepository extends JpaRepository<MotionMusic, Integer> {

    List<MotionMusic> findByBaseMusicId(Integer baseMusicId);
}
