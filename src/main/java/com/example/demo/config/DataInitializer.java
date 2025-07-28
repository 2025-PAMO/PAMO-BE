package com.example.demo.config;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

// Explore, my-music 테스트용 데이터 생성 코드
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MotionMusicRepository motionMusicRepository;
    private final MotionMusicLikeRepository motionMusicLikeRepository;
    private final BaseMusicRepository baseMusicRepository;
    private final BaseMusicLikeRepository baseMusicLikeRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws InterruptedException {
        // 1. 사용자 생성
        User user1 = newUser("Alice", "https://example.com/alice.png", "KAKAO");
        User user2 = newUser("Bob", "https://example.com/bob.png", "GOOGLE");
        User user3 = newUser("Charlie", "https://example.com/charlie.png", "NAVER");

        userRepository.saveAll(List.of(user1, user2, user3));
        userRepository.flush(); // ID 보장

        // 2. 음악 생성
        List<MotionMusic> musics = new ArrayList<>();
        musics.add(newMusic("Midnight Dream", user1, 120, true, "https://example.com/cover1.jpg"));
        motionMusicRepository.saveAndFlush(musics.get(0));
        Thread.sleep(1000);
        musics.add(newMusic("Ocean Vibes", user2, 240, true, "https://example.com/cover2.jpg"));
        motionMusicRepository.saveAndFlush(musics.get(musics.size() - 1));
        Thread.sleep(1000);
        musics.add(newMusic("Silent Echo", user1, 50, false, "https://example.com/cover3.jpg")); // 비공개
        motionMusicRepository.saveAndFlush(musics.get(musics.size() - 1));
        Thread.sleep(1000);
        musics.add(newMusic("Golden Hour", user3, 300, true, "https://example.com/cover4.jpg"));
        motionMusicRepository.saveAndFlush(musics.get(musics.size() - 1));
        Thread.sleep(1000);
        musics.add(newMusic("Electric Sky", user1, 80, true, "https://example.com/cover5.jpg"));
        motionMusicRepository.saveAndFlush(musics.get(musics.size() - 1));
        Thread.sleep(1000);
        musics.add(newMusic("Rainy Days", user2, 190, true, "https://example.com/cover6.jpg"));
        motionMusicRepository.saveAndFlush(musics.get(musics.size() - 1));

        // 3. 좋아요 생성
        List<MotionMusicLike> likes = new ArrayList<>();
        likes.add(newLike(user1, musics.get(1), "session1")); // Alice → Ocean Vibes
        likes.add(newLike(user2, musics.get(0), "session2")); // Bob → Midnight Dream
        likes.add(newLike(user2, musics.get(4), "session3")); // Bob → Electric Sky
        likes.add(newLike(user3, musics.get(0), "session4")); // Charlie → Midnight Dream
        likes.add(newLike(user3, musics.get(1), "session5")); // Charlie → Ocean Vibes
        likes.add(newLike(user3, musics.get(3), "session6")); // Charlie → Golden Hour
        likes.add(newLike(user1, musics.get(3), "session7")); // Alice → Golden Hour
        likes.add(newLike(user2, musics.get(3), "session8")); // Bob → Golden Hour
        motionMusicLikeRepository.saveAll(likes);

        // ========== my-music API 테스트용 데이터 추가 ==========
        // 사용자별 motion/base 음악 구분 및 즐겨찾기 상태 설정용

        // MotionMusic 추가 (Alice)
        MotionMusic motion1 = newMusic("Alice's Motion 1", user1, 100, true, "https://example.com/alice_motion1.jpg");
        MotionMusic motion2 = newMusic("Alice's Motion 2", user1, 150, false, "https://example.com/alice_motion2.jpg");
        motionMusicRepository.saveAll(List.of(motion1, motion2));

        // MotionMusic 추가 (Bob)
        MotionMusic motion3 = newMusic("Bob's Motion 1", user2, 80, true, "https://example.com/bob_motion1.jpg");
        motionMusicRepository.save(motion3);

        // BaseMusic 추가
        BaseMusic base1 = new BaseMusic();
        base1.setTitle("Base Track A");
        base1.setFileUrl("https://example.com/base_a.mp3");
        base1.setUserId(user1.getId());
        baseMusicRepository.save(base1);

        BaseMusic base2 = new BaseMusic();
        base2.setTitle("Base Track B");
        base2.setFileUrl("https://example.com/base_b.mp3");
        base2.setUserId(user1.getId());
        baseMusicRepository.save(base2);

        BaseMusic base3 = new BaseMusic();
        base3.setTitle("Base Track C");
        base3.setFileUrl("https://example.com/base_c.mp3");
        base3.setUserId(user2.getId());
        baseMusicRepository.save(base3);

        baseMusicRepository.flush();

        // BaseMusicLike 설정 (즐겨찾기 테스트용)
        BaseMusicLike like1 = new BaseMusicLike();
        like1.setUser(user1); // Alice가 base1을 북마크
        like1.setBaseMusic(base1);

        BaseMusicLike like2 = new BaseMusicLike();
        like2.setUser(user2); // Bob이 base2를 북마크
        like2.setBaseMusic(base2);

        baseMusicLikeRepository.saveAll(List.of(like1, like2));

        base1.setLikes(List.of(like1));
        base2.setLikes(List.of(like2));
        baseMusicLikeRepository.saveAll(List.of(like1, like2));

        System.out.println("✅ 예시 데이터 생성 완료");
    }

    private User newUser(String nickname, String profileImage, String joinType) {
        User user = new User();
        user.setNickname(nickname);
        user.setProfileImage(profileImage);
        user.setJoinType(joinType);
        return user;
    }

    private MotionMusic newMusic(String title, User user, int count, boolean visibility, String coverUrl) {
        MotionMusic music = new MotionMusic();
        music.setTitle(title);
        music.setUser(user);
        music.setUserId(user.getId()); // user 저장 이후 ID 할당됨
        music.setCount(count);
        music.setVisibility(visibility);
        music.setCover(coverUrl);
        return music;
    }

    private MotionMusicLike newLike(User user, MotionMusic music, String sessionId) {
        MotionMusicLike like = new MotionMusicLike();
        like.setUser(user);
        like.setMotionMusic(music);
        like.setSessionId(sessionId);
        return like;
    }
}
