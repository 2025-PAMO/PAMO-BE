package com.example.demo.config;

import com.example.demo.domain.*;
import com.example.demo.oauth.entity.ProviderType;
import com.example.demo.oauth.entity.RoleType;
import com.example.demo.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MotionMusicRepository motionMusicRepository;
    private final MotionMusicLikeRepository motionMusicLikeRepository;
    private final BaseMusicRepository baseMusicRepository;
    private final BaseMusicLikeRepository baseMusicLikeRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws InterruptedException {
        // 이미 다른 데이터(User, BaseMusic 등)가 있으면 스킵
        if (isAlreadySeeded()) {
            log.info("✅ Seed skipped: DB에 기존 데이터가 있어 시드를 수행하지 않습니다.");
        } else {
            // 유저, 베이스뮤직, 모션뮤직 등 기존 시드 로직 그대로...
            User user1 = newUser("alice_kakao_id", "Alice", "https://example.com/alice.png", "KAKAO");
            User user2 = newUser("bob_google_id", "Bob", "https://example.com/bob.png", "GOOGLE");
            User user3 = newUser("charlie_naver_id", "Charlie", "https://example.com/charlie.png", "NAVER");
            userRepository.saveAll(List.of(user1, user2, user3));
            userRepository.flush();

            BaseMusic base1 = newBaseMusic("Base Track A", "https://example.com/base_a.mp3", user1, "session_a");
            BaseMusic base2 = newBaseMusic("Base Track B", "https://example.com/base_b.mp3", user1, "session_b");
            BaseMusic base3 = newBaseMusic("Base Track C", "https://example.com/base_c.mp3", user2, "session_c");
            baseMusicRepository.saveAll(List.of(base1, base2, base3));
            baseMusicRepository.flush();

            List<MotionMusic> musics = new ArrayList<>();
            musics.add(newMotionMusic("Midnight Dream", user1, base1, 120, true, "https://example.com/cover1.jpg", "session_md"));
            musics.add(newMotionMusic("Ocean Vibes", user2, base2, 240, true, "https://example.com/cover2.jpg", "session_ov"));
            musics.add(newMotionMusic("Silent Echo", user1, base3, 50, false, "https://example.com/cover3.jpg", "session_se"));
            musics.add(newMotionMusic("Golden Hour", user3, base1, 300, true, "https://example.com/cover4.jpg", "session_gh"));
            musics.add(newMotionMusic("Electric Sky", user1, base2, 80, true, "https://example.com/cover5.jpg", "session_es"));
            musics.add(newMotionMusic("Rainy Days", user2, base3, 190, true, "https://example.com/cover6.jpg", "session_rd"));

            for (MotionMusic music : musics) {
                motionMusicRepository.saveAndFlush(music);
                Thread.sleep(200);
            }

            motionMusicLikeRepository.saveAll(List.of(
                    newMotionLike(user1, musics.get(1), "session1"),
                    newMotionLike(user2, musics.get(0), "session2"),
                    newMotionLike(user2, musics.get(4), "session3"),
                    newMotionLike(user3, musics.get(0), "session4"),
                    newMotionLike(user3, musics.get(1), "session5"),
                    newMotionLike(user3, musics.get(3), "session6"),
                    newMotionLike(user1, musics.get(3), "session7"),
                    newMotionLike(user2, musics.get(3), "session8")
            ));

            baseMusicLikeRepository.saveAll(List.of(
                    newBaseMusicLike(user1, base1, "base_like1"),
                    newBaseMusicLike(user2, base2, "base_like2")
            ));
        }

        // ✅ music_summary는 항상 Insert 실행 (중복 session_id는 무시)
        entityManager.createNativeQuery("""
            INSERT IGNORE INTO music_summary (session_id, base_music_id, created_at, summary_text) VALUES
            ('session_001', NULL, CURRENT_TIMESTAMP, '분위기: 따뜻하고 차분한 장르: 재즈 목적: 독서할 때 듣는 음악 주요 악기: 피아노'),
            ('session_002', NULL, CURRENT_TIMESTAMP, '분위기: 에너제틱하고 신나는 장르: EDM (일렉트로닉) 목적: 운동할 때 듣는 음악 주요 악기: 신디사이저'),
            ('session_003', NULL, CURRENT_TIMESTAMP, '분위기: 차분하고 몽환적인 장르: lo-fi 목적: 공부/집중할 때 듣는 음악 주요 악기: 일렉트릭 기타'),
            ('session_004', NULL, CURRENT_TIMESTAMP, '분위기: 밝고 따뜻한 장르: 발라드 목적: 선물용 음악 주요 악기: 어쿠스틱 기타'),
            ('session_005', NULL, CURRENT_TIMESTAMP, '분위기: 어둡고 강렬한 장르: 락 목적: 드라이브 음악 주요 악기: 드럼'),
            ('session_006', NULL, CURRENT_TIMESTAMP, '분위기: 밝고 부드러운, 기타 소리가 강조된, 밝고 경쾌한 장르: Pop 목적: 휴식할 때 듣기 좋은 음악'),
            ('session_007', NULL, CURRENT_TIMESTAMP, '분위기: 에너제틱하고 신나는 장르: 댄스팝 목적: 파티와 축제에서 듣기 좋은 음악 주요 악기: 신디사이저, 드럼 — 밝고 신나는 댄스팝, 중독성 있는 멜로디와 강렬한 비트, 축제 분위기'),
            ('session_008', NULL, CURRENT_TIMESTAMP, '분위기: 강렬하고 역동적인 장르: EDM (하우스) 목적: 클럽에서 춤추기 좋은 음악 주요 악기: 베이스, 킥드럼, 신디사이저 — 빠른 비트와 묵직한 베이스, 신나는 드롭과 리프'),
            ('session_009', NULL, CURRENT_TIMESTAMP, '분위기: 화려하고 에너제틱한 장르: 일렉트로닉 댄스 목적: 페스티벌 공연용 음악 주요 악기: 신스 패드, 드럼머신 — 강렬한 드럼과 베이스라인, 밝고 화려한 신스 사운드, 페스티벌 무대 분위기'),
            ('session_010', NULL, CURRENT_TIMESTAMP, '분위기: 신나는 리듬감 장르: K-댄스팝 목적: 운동할 때 에너지 업 음악 주요 악기: 전자드럼, EDM 신디사이저 — 빠른 템포와 경쾌한 리듬, 파워풀한 클럽 사운드'),
            ('session_011', NULL, CURRENT_TIMESTAMP, '분위기: 활기차고 희망적인 장르: 댄스팝 목적: 아침에 하루를 시작할 때 듣는 음악 주요 악기: 신디사이저, 일렉트릭 기타 — 경쾌한 멜로디와 리드미컬한 기타 리프, 밝고 긍정적인 에너지'),
            ('session_012', NULL, CURRENT_TIMESTAMP, '분위기: 화려하고 중독성 있는 장르: EDM (하우스) 목적: 클럽에서 무대용 음악 주요 악기: 베이스, 드럼머신, 신디사이저 — 반복적인 리듬과 드롭, 묵직한 베이스, 신나는 하우스 EDM'),
            ('session_013', NULL, CURRENT_TIMESTAMP, '분위기: 에너지 넘치고 다채로운 장르: K-댄스팝 목적: 공연/댄스 챌린지용 음악 주요 악기: 전자드럼, 브라스 신디사이저 — 강렬한 드럼과 화려한 신스 브라스 사운드, 댄스 퍼포먼스에 어울림'),
            ('session_014', NULL, CURRENT_TIMESTAMP, '분위기: 즐겁고 발랄한 장르: 일렉트로닉 댄스 목적: 친구들과 파티할 때 듣는 음악 주요 악기: 신스 패드, 킥드럼 — 밝은 신스 코드와 경쾌한 리듬, 파티에 어울림'),
            ('session_015', NULL, CURRENT_TIMESTAMP, '분위기: 파워풀하고 다이내믹한 장르: 댄스 EDM 목적: 운동이나 러닝할 때 에너지 충전 주요 악기: 전자드럼, 베이스, 신디사이저 — 빠른 템포와 강렬한 드럼, 강력한 베이스라인');
        """).executeUpdate();

        log.info("✅ music_summary 시드 완료 (항상 실행, session_001 ~ session_015)");
    }
    private boolean isAlreadySeeded() {
        return userRepository.count() > 0L
                || baseMusicRepository.count() > 0L
                || motionMusicRepository.count() > 0L
                || baseMusicLikeRepository.count() > 0L
                || motionMusicLikeRepository.count() > 0L;
    }

    private User newUser(String userId, String nickname, String profileImage, String joinType) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail(nickname.toLowerCase() + "@test.com");
        user.setNickname(nickname);
        user.setProfileImage(profileImage);
        user.setProviderType(ProviderType.valueOf(joinType));
        user.setRoleType(RoleType.USER);
        user.setEmailVerifiedYn("Y");
        user.setCreatedAt(LocalDateTime.now());
        user.setModifiedAt(LocalDateTime.now());
        return user;
    }

    private BaseMusic newBaseMusic(String title, String url, User user, String sessionId) {
        BaseMusic base = new BaseMusic();
        base.setTitle(title);
        base.setFileUrl(url);
        base.setUser(user);
        base.setSessionId(sessionId);
        return base;
    }

    private MotionMusic newMotionMusic(String title, User user, BaseMusic baseMusic, int count, boolean visibility, String coverUrl, String sessionId) {
        MotionMusic motion = new MotionMusic();
        motion.setTitle(title);
        motion.setUser(user);
        motion.setBaseMusic(baseMusic);
        motion.setCount(count);
        motion.setVisibility(visibility);
        motion.setCover(coverUrl);
        motion.setFileUrl("https://example.com/" + title.toLowerCase().replace(" ", "_") + ".mp3");
        motion.setAniUrl("https://example.com/" + title.toLowerCase().replace(" ", "_") + ".json");
        motion.setSessionId(sessionId);
        return motion;
    }

    private MotionMusicLike newMotionLike(User user, MotionMusic music, String sessionId) {
        MotionMusicLike like = new MotionMusicLike();
        like.setUser(user);
        like.setMotionMusic(music);
        like.setSessionId(sessionId);
        return like;
    }

    private BaseMusicLike newBaseMusicLike(User user, BaseMusic base, String sessionId) {
        BaseMusicLike like = new BaseMusicLike();
        like.setUser(user);
        like.setBaseMusic(base);
        like.setSessionId(sessionId);
        return like;
    }
}
