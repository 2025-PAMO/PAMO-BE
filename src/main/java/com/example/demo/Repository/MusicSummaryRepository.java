package com.example.demo.Repository;

import com.example.demo.Entity.MusicSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MusicSummaryRepository extends JpaRepository<MusicSummary, String> {
    Optional<MusicSummary> findBySessionId(String sessionId);

    boolean existsBySessionId(String sessionId);
}

