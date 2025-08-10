package com.example.demo.repository;

import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.MotionMusicLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MotionMusicLikeRepository extends JpaRepository<MotionMusicLike, Integer> {

    // 사용자가 좋아요한 시간(l.createdAt) 기준 최신순, 공개된 음악만, 상위 N개
    @Query("""
        SELECT m
        FROM MotionMusicLike l
        JOIN l.motionMusic m
        WHERE l.user.id = :userId
          AND m.visibility = true
        ORDER BY l.createdAt DESC
    """)
    List<MotionMusic> findUserLikedVisibleMusicOrderByLikedAtDesc(
            @Param("userId") Integer userId,
            Pageable pageable
    );

    // 사용자가 좋아요한 시간(l.createdAt) 기준 과거순, 공개된 음악 전부
    @Query("""
        SELECT m
        FROM MotionMusicLike l
        JOIN l.motionMusic m
        WHERE l.user.id = :userId
          AND m.visibility = true
        ORDER BY l.createdAt ASC
    """)
    List<MotionMusic> findAllUserLikedVisibleMusicOrderByLikedAtAsc(
            @Param("userId") Integer userId
    );

    boolean existsByUserIdAndMotionMusicId(Integer userId, Integer motionMusicId);

}
