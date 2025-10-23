package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.music.MusicDetailResponseDTO;
import com.example.demo.dto.music.TitleUpdateRequestDTO;
import com.example.demo.dto.music.VisibilityUpdateRequestDTO;
import com.example.demo.service.MotionMusicService;
import com.example.demo.service.MusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.util.SecurityUtil.getCurrentUserId;
import static com.example.demo.util.SecurityUtil.getCurrentUserIdOrNull;

@Tag(name = "모션 음악 API", description = "모션 음악 관리 API (공개 여부, 제목, 삭제, 좋아요, 상세)")
@RestController
@RequestMapping("/api/motion-music")
@RequiredArgsConstructor
public class MotionMusicController {
    private final MotionMusicService motionMusicService;
    private final MusicService musicService;

    @Operation(summary = "모션 음악 공개 여부 변경", description = "모션 음악의 공개/비공개 상태를 변경합니다.")
    @PatchMapping("/{id}/visibility")
    public CustomResponse<String> updateVisibility(
            @PathVariable("id") Integer musicId,
            @RequestBody VisibilityUpdateRequestDTO requestDTO
    ){
        Integer userId = getCurrentUserId();
        motionMusicService.updateVisibility(userId, musicId, requestDTO.getVisibility());
        return CustomResponse.onSuccess("모션 음악 공개 여부가 성공적으로 변경되었습니다.");
    }

    @Operation(summary = "모션 음악 제목 수정", description = "모션 음악의 제목을 변경합니다.")
    @PatchMapping("/{id}/title")
    public CustomResponse<String> updateMotionMusicTitle(
            @PathVariable("id") Integer musicId,
            @RequestBody TitleUpdateRequestDTO requestDTO
    ){
        Integer userId = getCurrentUserId();
        motionMusicService.updateMotionMusicTitle(userId, musicId, requestDTO.getTitle());
        return CustomResponse.onSuccess("모션 음악 제목이 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "모션 음악 삭제", description = "모션 음악을 삭제합니다.")
    @DeleteMapping("/{id}")
    public CustomResponse<String> deleteMotionMusic(@PathVariable("id") Integer musicId) {
        Integer userId = getCurrentUserId();
        motionMusicService.deleteMotionMusic(userId, musicId);
        return CustomResponse.onSuccess("모션 음악이 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "모션 음악 좋아요 추가", description = "모션 음악을 좋아요 목록에 추가합니다.")
    @PutMapping("/{id}/like")
    public CustomResponse<String> like(@PathVariable("id") Integer id) {
        motionMusicService.like(getCurrentUserId(), id);
        return CustomResponse.onSuccess("모션 음악을 성공적으로 좋아요 목록에 추가했습니다.");
    }

    @Operation(summary = "모션 음악 좋아요 취소", description = "모션 음악 좋아요를 취소합니다.")
    @DeleteMapping("/{id}/like")
    public CustomResponse<String> unlike(@PathVariable("id") Integer musicId) {
        Integer userId = getCurrentUserId();
        motionMusicService.unlike(userId, musicId);
        return CustomResponse.onSuccess("모션 음악을 성공적으로 좋아요 목록에서 삭제했습니다.");
    }

    @Operation(summary = "모션 음악 상세 조회", description = "컨텍스트(explore|mypage)에 따라 상세 정보를 조회합니다.", security = {})
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
