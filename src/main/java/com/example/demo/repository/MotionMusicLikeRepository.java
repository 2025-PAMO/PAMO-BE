package com.example.demo.repository;

import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.MotionMusicLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MotionMusicLikeRepository extends JpaRepository<MotionMusicLike, Integer> {

    // 둘러보기(좋아요한 음악 최신 3개에 사용): 내가 좋아요한 공개 모션 음악 + 내가 만든 비공개 모션 음악 중 내가 좋아요한 음악, 최신순 + 페이지네이션
    @Query("""
        SELECT m
        FROM MotionMusicLike l
        JOIN l.motionMusic m
        WHERE l.user.id = :userId
          AND (m.visibility = true OR m.user.id = :userId)
        ORDER BY l.createdAt DESC
    """)
    List<MotionMusic> findUserLikedVisibleOrOwnedPrivateOrderByLikedAtDesc(
            @Param("userId") Integer userId,
            Pageable pageable
    );

    // 라이브러리: 내가 좋아요한 공개곡 + 내가 만든 비공개곡(내가 좋아요한 것만), 과거순
    @Query("""
        SELECT m
        FROM MotionMusicLike l
        JOIN l.motionMusic m
        WHERE l.user.id = :userId
          AND (m.visibility = true OR m.user.id = :userId)
        ORDER BY l.createdAt ASC
    """)
    List<MotionMusic> findAllUserLikedVisibleOrOwnedPrivateOrderByLikedAtAsc(
            @Param("userId") Integer userId
    );

    boolean existsByUserIdAndMotionMusicId(Integer userId, Integer motionMusicId);
    void deleteByUserIdAndMotionMusicId(Integer userId, Integer motionMusicId);

}
