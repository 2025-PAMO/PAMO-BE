package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "base_music_like")
public class BaseMusicLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_music_id", nullable = false)
    private BaseMusic baseMusic;

    @Column(name = "created_at", columnDefinition = "TIME", insertable = false, updatable = false)
    private Time createdAt;

    @Column(name = "session_id", length = 64)
    private String sessionId;
}
