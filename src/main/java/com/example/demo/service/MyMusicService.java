package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.baseMusic.BaseMusicDTO;
import com.example.demo.dto.motionMusic.MotionMusicDTO;
import com.example.demo.dto.myMusic.MyMusicResponseDTO;
import com.example.demo.dto.user.UserDto;
import com.example.demo.repository.BaseMusicRepository;
import com.example.demo.repository.MotionMusicRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class MyMusicService {
    private final UserRepository userRepository;
    private final MotionMusicRepository motionMusicRepository;
    private final BaseMusicRepository baseMusicRepository;

    public MyMusicResponseDTO getMyMusic(Integer userId, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserDto userProfile = UserDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .build();

        if ("motion".equalsIgnoreCase(type)) {
            List<MotionMusicDTO> motionMusicList = motionMusicRepository.findByUser(user)
                    .stream()
                    .map(m -> MotionMusicDTO.builder()
                            .motionMusicId(m.getId())
                            .title(m.getTitle())
                            .artist(user.getNickname())
                            .coverImageUrl(m.getCover())
                            .likeCount(m.getLikes() != null ? m.getLikes().size() : 0)
                            .visibility(m.getVisibility() != null && m.getVisibility())
                            .build())
                    .collect(Collectors.toList());

            return MyMusicResponseDTO.builder()
                    .user(userProfile)
                    .type("motion")
                    .myMusicList(motionMusicList)
                    .build();

        } else if ("base".equalsIgnoreCase(type)) {
            List<BaseMusicDTO> baseMusicList = baseMusicRepository.findByUser(user)
                    .stream()
                    .map(b -> BaseMusicDTO.builder()
                            .baseMusicId(b.getId())
                            .title(b.getTitle())
                            .artist(user.getNickname())
                            .musicFileUrl(b.getFileUrl())
                            .isBookmarked(b.getLikes().stream().anyMatch(like -> like.getUser().getId().equals(user.getId())))
                            .build())
                    .collect(Collectors.toList());
            
            return MyMusicResponseDTO.builder()
                    .user(userProfile)
                    .type("base")
                    .myMusicList(baseMusicList)
                    .build();

        } else {
            throw new IllegalArgumentException("잘못된 type 값입니다. (motion | base)");
        }
    }
}
