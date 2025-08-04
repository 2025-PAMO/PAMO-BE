package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "motion_music")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotionMusic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_music_id", nullable = false)
    private BaseMusic baseMusic;

    @Column(name = "base_title")
    private String baseTitle;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    private String title;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "ani_url")
    private String aniUrl;

    private Integer count;

    private Boolean visibility;

    @OneToMany(mappedBy = "motionMusic", cascade = CascadeType.ALL)
    private List<MotionMusicLike> likes;


    @Column(name = "created_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    private String cover;
}
