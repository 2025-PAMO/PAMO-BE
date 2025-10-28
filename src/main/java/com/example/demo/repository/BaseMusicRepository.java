package com.example.demo.repository;

import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.User;
import com.example.demo.repository.projection.MusicSearchProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BaseMusicRepository extends JpaRepository<BaseMusic, Integer> {

    long countByUserId(Integer userId);
    List<BaseMusic> findByUser(User user);

    // ✅ isDeleted → deletable
    Optional<BaseMusic> findBySessionIdAndDeletableFalse(String sessionId);
    Optional<BaseMusic> findByIdAndDeletableFalse(Integer id);

    Optional<BaseMusic> findBySessionId(String sessionId);
}
