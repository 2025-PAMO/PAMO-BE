package com.example.demo.repository;

import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MotionMusicRepository extends JpaRepository<MotionMusic, Integer> {

    // 공개된 음악 중 재생 수 내림차순
    List<MotionMusic> findByVisibilityTrueOrderByCountDesc(Pageable pageable);

    // 공개된 음악 중 최신순
    List<MotionMusic> findByVisibilityTrueOrderByCreatedAtDesc(Pageable pageable);

    // 공개된 음악 중 좋아요 수 내림차순
    @Query("""
        SELECT m FROM MotionMusic m
        LEFT JOIN m.likes l
        WHERE m.visibility = true
        GROUP BY m.id
        ORDER BY COUNT(l) DESC
    """)
    List<MotionMusic> findMostLikedVisibleMotionMusic(Pageable pageable);

    List<MotionMusic> findByUser(User user);

    List<MotionMusic> findByBaseMusicId(Integer baseMusicId);
}
