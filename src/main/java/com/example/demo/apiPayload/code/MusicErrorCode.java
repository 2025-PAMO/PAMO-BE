package com.example.demo.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MusicErrorCode implements BaseErrorCode {

    BASE_MUSIC_NOT_DELETABLE(HttpStatus.BAD_REQUEST, "MUSIC400", "연결된 모션 음악이 모두 삭제되지 않아 삭제할 수 없습니다."),
    MUSIC_NOT_FOUND(HttpStatus.NOT_FOUND, "MUSIC404", "해당 음악이 존재하지 않습니다."),
    NO_PERMISSION(HttpStatus.FORBIDDEN, "MUSIC403", "해당 음악을 수정하거나 삭제할 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
