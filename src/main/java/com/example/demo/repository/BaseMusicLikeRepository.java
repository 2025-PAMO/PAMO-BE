package com.example.demo.repository;

import com.example.demo.domain.BaseMusicLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseMusicLikeRepository extends JpaRepository<BaseMusicLike, Integer> {
}
