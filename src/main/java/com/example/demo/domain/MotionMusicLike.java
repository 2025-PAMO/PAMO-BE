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
@Table(name = "motion_music_like")
public class MotionMusicLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motion_music_id", nullable = false)
    private MotionMusic motionMusic;
    //베이스뮤직
    //베이스뮤직like
    //모션뮤직like

    @Column(name = "created_at", columnDefinition = "TIME", insertable = false, updatable = false)
    private Time createdAt;

    @Column(name = "session_id", length = 64)
    private String sessionId;
}
