package com.example.demo.repository;

import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.User;
import com.example.demo.repository.projection.MusicSearchProjection;
import com.example.demo.repository.projection.RelatedItemView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

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

    long countByUserId(Integer userId); // 🔹 기본 제목용 카운트

    @Query("""
      SELECT mm FROM MotionMusic mm
        JOIN FETCH mm.user u
        JOIN FETCH mm.baseMusic bm
      WHERE mm.id = :id
    """)
    Optional<MotionMusic> findByIdWithOwnerAndBase(@Param("id") Integer id);

    // 해당 BaseMusic으로 만든 공개 모션음악 작성자 프로필 이미지 3개 (중복 제거)
    @Query("""
      SELECT DISTINCT u.profileImage
      FROM MotionMusic mm
      JOIN mm.user u
      WHERE mm.baseMusic.id = :baseId
        AND mm.visibility = TRUE
      ORDER BY mm.createdAt DESC
    """)
    List<String> findDistinctCreatorProfileImagesByBase(@Param("baseId") Integer baseId,
                                                        Pageable pageable);


    // related 1순위: 같은 base music의 다른 공개 모션음악 (현재 곡 제외)
    @Query("""
      SELECT 
        mm.id          AS id,
        mm.title       AS title,
        u.nickname     AS artist,
        mm.cover       AS coverImageUrl,
        mm.count       AS viewCount
      FROM MotionMusic mm
      JOIN mm.user u
      WHERE mm.id <> :currentId
        AND mm.visibility = TRUE
        AND mm.baseMusic.id = :baseId
      ORDER BY mm.count DESC, mm.createdAt DESC
    """)
    List<RelatedItemView> findRelatedPrimary(
            @Param("currentId") Integer currentId,
            @Param("baseId") Integer baseId,
            Pageable pageable);

    // related 보충: 다른 base music의 공개 모션음악 (현재 곡 제외)
    @Query("""
      SELECT 
        mm.id          AS id,
        mm.title       AS title,
        u.nickname     AS artist,
        mm.cover       AS coverImageUrl,
        mm.count       AS viewCount
      FROM MotionMusic mm
      JOIN mm.user u
      WHERE mm.id <> :currentId
        AND mm.visibility = TRUE
        AND mm.baseMusic.id <> :baseId
      ORDER BY mm.count DESC, mm.createdAt DESC
    """)
    List<RelatedItemView> findRelatedFallback(
            @Param("currentId") Integer currentId,
            @Param("baseId") Integer baseId,
            Pageable pageable);

    @Query(value = """
    SELECT m.*
    FROM motion_music m
    JOIN users u ON m.user_id = u.id
    WHERE m.visibility = true
      AND (
        m.title COLLATE utf8mb4_general_ci LIKE CONCAT('%', :q, '%')
        OR u.nickname COLLATE utf8mb4_general_ci LIKE CONCAT('%', :q, '%')
      )
    ORDER BY 
      CASE 
        WHEN m.title COLLATE utf8mb4_general_ci LIKE CONCAT(:q, '%') THEN 3
        WHEN m.title COLLATE utf8mb4_general_ci LIKE CONCAT('%', :q, '%') THEN 2
        ELSE 1
      END DESC,
      CASE WHEN :sort = 'recent' THEN m.created_at END DESC,
      m.created_at DESC
    """, nativeQuery = true)
    List<MotionMusic> searchMotion(@Param("q") String q,
                                   @Param("sort") String sort,
                                   Pageable pageable);


}


