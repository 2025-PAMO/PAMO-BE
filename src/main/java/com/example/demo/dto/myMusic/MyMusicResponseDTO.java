package com.example.demo.dto.myMusic;

import com.example.demo.dto.user.UserProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyMusicResponseDTO {
    private UserProfileDTO user;
    private String type;
    private List<?> myMusicList;  // MotionMusicDTO 또는 BaseMusicDTO
}
