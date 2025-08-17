package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.music.MusicDetailResponseDTO;
import com.example.demo.dto.music.TitleUpdateRequestDTO;
import com.example.demo.dto.music.VisibilityUpdateRequestDTO;
import com.example.demo.service.MotionMusicService;
import com.example.demo.service.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.util.SecurityUtil.getCurrentUserId;
import static com.example.demo.util.SecurityUtil.getCurrentUserIdOrNull;

@RestController
@RequestMapping("/api/motion-music")
@RequiredArgsConstructor
public class MotionMusicController {
    private final MotionMusicService motionMusicService;
    private final MusicService musicService;

    @PatchMapping("/{id}/visibility")
    public CustomResponse<String> updateVisibility(
            @PathVariable("id") Integer musicId,
            @RequestBody VisibilityUpdateRequestDTO requestDTO
    ){
        Integer userId = getCurrentUserId();
        motionMusicService.updateVisibility(userId, musicId, requestDTO.getVisibility());
        return CustomResponse.onSuccess("모션 음악 공개 여부가 성공적으로 변경되었습니다.");
    }

    @PatchMapping("/{id}/title")
    public CustomResponse<String> updateMotionMusicTitle(
            @PathVariable("id") Integer musicId,
            @RequestBody TitleUpdateRequestDTO requestDTO
    ){
        Integer userId = getCurrentUserId();
        motionMusicService.updateMotionMusicTitle(userId, musicId, requestDTO.getTitle());
        return CustomResponse.onSuccess("모션 음악 제목이 성공적으로 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public CustomResponse<String> deleteMotionMusic(@PathVariable("id") Integer musicId) {
        Integer userId = getCurrentUserId();
        motionMusicService.deleteMotionMusic(userId, musicId);
        return CustomResponse.onSuccess("모션 음악이 성공적으로 삭제되었습니다.");
    }

    @PutMapping("/{id}/like")
    public CustomResponse<String> like(@PathVariable("id") Integer id) {
        motionMusicService.like(getCurrentUserId(), id);
        return CustomResponse.onSuccess("모션 음악을 성공적으로 좋아요 목록에 추가했습니다.");
    }

    @DeleteMapping("/{id}/like")
    public CustomResponse<String> unlike(@PathVariable("id") Integer musicId) {
        Integer userId = getCurrentUserId();
        motionMusicService.unlike(userId, musicId);
        return CustomResponse.onSuccess("모션 음악을 성공적으로 좋야요 목록에서 삭제했습니다.");
    }

    @GetMapping("/{id}/detail")
    public CustomResponse<MusicDetailResponseDTO> getMusicDetail(
            @PathVariable Integer id,
            @RequestParam(name = "context") String context // "explore" | "mypage"
    ){
        if (!"explore".equals(context) && !"mypage".equals(context)) {
            return CustomResponse.onFailure("400", "context는 explore 또는 mypage여야 합니다.");
        }

        Integer viewerId = getCurrentUserIdOrNull();

        if ("mypage".equals(context) && viewerId == null) {
            return CustomResponse.onFailure("401", "context가 mypage일 때는 인증이 필요합니다.");
        }

        MusicDetailResponseDTO responseDTO = musicService.getDetail(id, context, viewerId);
        return CustomResponse.onSuccess(responseDTO);
    }
}
