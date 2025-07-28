package com.example.demo.repository;

import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BaseMusicRepository extends JpaRepository<BaseMusic, Integer> {
    long countByUserId(Integer userId);
    List<BaseMusic> findByUser(User user);
}

