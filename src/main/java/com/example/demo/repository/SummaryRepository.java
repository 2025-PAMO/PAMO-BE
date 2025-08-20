package com.example.demo.repository;

import com.example.demo.domain.MusicSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<MusicSummary, Long> {
    Optional<MusicSummary> findBySessionId(String sessionId);

    boolean existsBySessionId(String sessionId);
}
