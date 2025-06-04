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
@Table(name = "likes")
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "user_id")
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motion_music_id", insertable = false, updatable = false)
    private MotionMusic motionMusic;

    @Column(name = "motion_music_id")
    private Integer motionMusicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id2", insertable = false, updatable = false)
    private BaseMusic baseMusic;

    @Column(name = "id2")
    private Integer baseMusicId;

    @Column(name = "created_at")
    private Time createdAt;

    @Column(name = "session_id", length = 64)
    private String sessionId;
}

