package com.example.demo.repository;

import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.User;
import com.example.demo.repository.projection.MusicSearchProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BaseMusicRepository extends JpaRepository<BaseMusic, Integer> {

    long countByUserId(Integer userId);
    List<BaseMusic> findByUser(User user);
    Optional<BaseMusic> findBySessionId(String sessionId);


    @Query(value = """
        SELECT 
          b.id AS id,
          b.title AS title,
          b.cover AS cover,
          COUNT(l.user_id) AS likeCount,
          NULL AS playCount
        FROM base_music b
        LEFT JOIN base_music_likes l ON l.base_music_id = b.id
        LEFT JOIN base_music_tag t ON t.base_music_id = b.id
        WHERE 
          LOWER(b.title) LIKE CONCAT('%', :q, '%')
          OR LOWER(COALESCE(b.description, '')) LIKE CONCAT('%', :q, '%')
          OR LOWER(t.tag) LIKE CONCAT('%', :q, '%')
        GROUP BY b.id, b.title, b.cover
        ORDER BY 
          CASE 
            WHEN LOWER(b.title) LIKE CONCAT(:q, '%') THEN 3
            WHEN LOWER(b.title) LIKE CONCAT('%', :q, '%') THEN 2
            ELSE 1
          END DESC,
          CASE WHEN :sort = 'popular' THEN COUNT(l.user_id) END DESC,
          CASE WHEN :sort = 'recent'  THEN b.created_at END DESC,
          b.created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<MusicSearchProjection> searchBase(@Param("q") String q,
                                           @Param("sort") String sort,
                                           @Param("limit") int limit);
}
