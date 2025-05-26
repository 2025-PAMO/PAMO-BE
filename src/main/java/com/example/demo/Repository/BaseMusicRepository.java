package com.example.demo.Repository;

import com.example.demo.Entity.BaseMusic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseMusicRepository extends JpaRepository<BaseMusic, Integer> {
}

