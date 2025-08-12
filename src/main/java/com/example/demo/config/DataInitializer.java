package com.example.demo.config;

import com.example.demo.domain.*;
import com.example.demo.oauth.entity.ProviderType;
import com.example.demo.oauth.entity.RoleType;
import com.example.demo.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//// Explore, my-music 테스트용 데이터 생성 코드
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MotionMusicRepository motionMusicRepository;
    private final MotionMusicLikeRepository motionMusicLikeRepository;
    private final BaseMusicRepository baseMusicRepository;
    private final BaseMusicLikeRepository baseMusicLikeRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager; // ✅ Native SQL 실행용

    @Override
    @Transactional
    public void run(String... args) throws InterruptedException {
        // 1. 사용자 생성
        User user1 = newUser("alice_kakao_id", "Alice", "https://example.com/alice.png", "KAKAO");
        User user2 = newUser("bob_google_id", "Bob", "https://example.com/bob.png", "GOOGLE");
        User user3 = newUser("charlie_naver_id", "Charlie", "https://example.com/charlie.png", "NAVER");
        userRepository.saveAll(List.of(user1, user2, user3));
        userRepository.flush();

        // 2. BaseMusic 생성 (MotionMusic에 필수)
        BaseMusic base1 = newBaseMusic("Base Track A", "https://example.com/base_a.mp3", user1, "session_a");
        BaseMusic base2 = newBaseMusic("Base Track B", "https://example.com/base_b.mp3", user1, "session_b");
        BaseMusic base3 = newBaseMusic("Base Track C", "https://example.com/base_c.mp3", user2, "session_c");
        baseMusicRepository.saveAll(List.of(base1, base2, base3));
        baseMusicRepository.flush();

        // 3. MotionMusic 생성
        List<MotionMusic> musics = new ArrayList<>();
        musics.add(newMotionMusic("Midnight Dream", user1, base1, 120, true, "https://example.com/cover1.jpg", "session_md"));
        musics.add(newMotionMusic("Ocean Vibes", user2, base2, 240, true, "https://example.com/cover2.jpg", "session_ov"));
        musics.add(newMotionMusic("Silent Echo", user1, base3, 50, false, "https://example.com/cover3.jpg", "session_se"));
        musics.add(newMotionMusic("Golden Hour", user3, base1, 300, true, "https://example.com/cover4.jpg", "session_gh"));
        musics.add(newMotionMusic("Electric Sky", user1, base2, 80, true, "https://example.com/cover5.jpg", "session_es"));
        musics.add(newMotionMusic("Rainy Days", user2, base3, 190, true, "https://example.com/cover6.jpg", "session_rd"));

        for (MotionMusic music : musics) {
            motionMusicRepository.saveAndFlush(music);
            Thread.sleep(500);
        }

        // 4. MotionMusicLike 생성
        List<MotionMusicLike> likes = List.of(
                newMotionLike(user1, musics.get(1), "session1"),
                newMotionLike(user2, musics.get(0), "session2"),
                newMotionLike(user2, musics.get(4), "session3"),
                newMotionLike(user3, musics.get(0), "session4"),
                newMotionLike(user3, musics.get(1), "session5"),
                newMotionLike(user3, musics.get(3), "session6"),
                newMotionLike(user1, musics.get(3), "session7"),
                newMotionLike(user2, musics.get(3), "session8")
        );
        motionMusicLikeRepository.saveAll(likes);

        // 5. My Music 테스트용 MotionMusic 추가
        motionMusicRepository.saveAll(List.of(
                newMotionMusic("Alice's Motion 1", user1, base1, 100, true, "https://example.com/alice_motion1.jpg", "session_am1"),
                newMotionMusic("Alice's Motion 2", user1, base1, 150, false, "https://example.com/alice_motion2.jpg", "session_am2"),
                newMotionMusic("Bob's Motion 1", user2, base3, 80, true, "https://example.com/bob_motion1.jpg", "session_bm1")
        ));

        // 6. BaseMusicLike 추가
        BaseMusicLike baseLike1 = newBaseMusicLike(user1, base1, "base_like1");
        BaseMusicLike baseLike2 = newBaseMusicLike(user2, base2, "base_like2");
        baseMusicLikeRepository.saveAll(List.of(baseLike1, baseLike2));

        // 7. ✅ music_summary 데이터 삽입
        entityManager.createNativeQuery("""
            INSERT INTO music_summary (session_id, base_music_id, created_at, summary_text) VALUES
            ('session_001', NULL, CURRENT_TIMESTAMP, '밝고 경쾌한 피아노 리프와 리드미컬한 드럼 비트가 어우러진 곡으로, 활기차고 긍정적인 분위기를 전달합니다.'),
            ('session_002', NULL, CURRENT_TIMESTAMP, '부드러운 어쿠스틱 기타와 잔잔한 스트링이 조화를 이루어 따뜻하고 차분한 감성을 자아냅니다.'),
            ('session_003', NULL, CURRENT_TIMESTAMP, '전자 신스 사운드와 묵직한 베이스라인이 결합된 곡으로, 신비롭고 미래지향적인 분위기를 연출합니다.'),
            ('session_004', NULL, CURRENT_TIMESTAMP, '서정적인 피아노 선율과 웅장한 오케스트라 사운드가 어우러져 영화의 한 장면 같은 감동을 줍니다.'),
            ('session_005', NULL, CURRENT_TIMESTAMP, '경쾌한 기타 리프와 강렬한 드럼이 중심이 된 록 스타일 곡으로, 에너지가 넘치고 역동적인 느낌을 줍니다.');
        """).executeUpdate();

        System.out.println("✅ 예시 데이터 생성 완료");
    }

    // --- Helper methods ---
    private User newUser(String userId, String nickname, String profileImage, String joinType) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail(nickname.toLowerCase() + "@test.com");
        user.setNickname(nickname);
        user.setProfileImage(profileImage);
        user.setJoinType(joinType);
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
