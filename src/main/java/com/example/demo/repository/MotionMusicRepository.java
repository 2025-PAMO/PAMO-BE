package com.example.demo.repository;

import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.User;
import com.example.demo.repository.projection.MusicSearchProjection;
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


    @Query(value = """
        SELECT 
          m.id AS id,
          m.title AS title,
          m.cover AS cover,
          COUNT(l.user_id) AS likeCount,
          m.count AS playCount
        FROM motion_music m
        LEFT JOIN motion_music_tag t ON t.motion_music_id = m.id
        LEFT JOIN motion_music_likes l ON l.motion_music_id = m.id
        WHERE m.visibility = true
          AND (
            LOWER(m.title) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(m.description, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(t.tag) LIKE CONCAT('%', :q, '%')
          )
        GROUP BY m.id, m.title, m.cover, m.count
        ORDER BY 
          CASE 
            WHEN LOWER(m.title) LIKE CONCAT(:q, '%') THEN 3
            WHEN LOWER(m.title) LIKE CONCAT('%', :q, '%') THEN 2
            ELSE 1
          END DESC,
          CASE WHEN :sort = 'popular' THEN COUNT(l.user_id) END DESC,
          CASE WHEN :sort = 'recent'  THEN m.created_at END DESC,
          m.created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<MusicSearchProjection> searchMotion(@Param("q") String q,
                                             @Param("sort") String sort,
                                             @Param("limit") int limit);
}

