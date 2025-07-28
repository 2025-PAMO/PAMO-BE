package com.example.demo.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="base_music")
public class BaseMusic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="session_id", length = 64)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "title")
    private String title;

    @Column(name = "file_url")
    private String fileUrl;

    @OneToMany(mappedBy = "baseMusic", cascade = CascadeType.ALL)
    private List<MotionMusic> motionMusics;

    @OneToMany(mappedBy = "baseMusic", cascade = CascadeType.ALL)
    private List<BaseMusicLike> likes;



}
