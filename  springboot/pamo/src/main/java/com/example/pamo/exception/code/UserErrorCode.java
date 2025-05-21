package com.example.pamo.exception.code;

import com.example.pamo.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404", "사람을 찾을 수 없습니다."),
    INVALID_VISIBILITY_CHANGE(HttpStatus.BAD_REQUEST, "USER400", "음악 공개 여부 변경 실패");

    private final HttpStatus status;
    private final String code;
    private final String message;
}