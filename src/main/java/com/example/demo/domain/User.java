package com.example.demo.domain;

import com.example.demo.oauth.entity.ProviderType;
import com.example.demo.oauth.entity.RoleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", length = 64, unique = true)
    private String userId; // id와 다른 점 : 소셜 로그인 제공자에서 받은 고유 ID

    @Column(name = "email", length = 512, unique = true)
    private String email;

    @Column(name = "email_verified_yn", length = 1)
    private String emailVerifiedYn; // "Y" 또는 "N"

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", length = 20)
    private ProviderType providerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", length = 20)
    private RoleType roleType;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<MotionMusic> motionMusics;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;


}
