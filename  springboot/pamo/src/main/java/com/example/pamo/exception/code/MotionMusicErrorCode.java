package com.example.pamo.exception.code;

import com.example.pamo.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MotionMusicErrorCode implements BaseErrorCode {
    MUSIC_NOT_FOUND(HttpStatus.NOT_FOUND, "MUSIC404", "모션 음악을 찾을 수 없습니다."),
    INVALID_VISIBILITY_CHANGE(HttpStatus.BAD_REQUEST, "MUSIC400", "음악 공개 여부 변경 실패");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
