package com.example.demo.repository;

import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.BaseMusicLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BaseMusicLikeRepository extends JpaRepository<BaseMusicLike, Integer> {
    @Query("""
        SELECT b
        FROM BaseMusicLike l
        JOIN l.baseMusic b
        WHERE l.user.id = :userId
        ORDER BY l.createdAt ASC
    """)
    List<BaseMusic> findAllUserLikedBaseMusicOrderByLikedAtAsc(
            @Param("userId") Integer userId
    );

}
