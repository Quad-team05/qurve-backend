package com.qurve.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(404, "USER_NOT_FOUND", "존재하지 않는 유저입니다."),
    DUPLICATE_LOGIN_ID(409, "DUPLICATE_LOGIN_ID", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(409, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(400, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(401, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "EXPIRED_TOKEN", "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(401, "UNSUPPORTED_TOKEN", "지원하지 않는 토큰입니다."),
    EMPTY_TOKEN(401, "EMPTY_TOKEN", "토큰이 비어있습니다."),
    EMAIL_SEND_FAIL(500, "EMAIL_SEND_FAIL", "이메일 발송에 실패했습니다."),
    INVALID_VERIFICATION_CODE(400, "INVALID_VERIFICATION_CODE", "인증코드가 일치하지 않습니다."),
    UNKNOWN_ERROR(500, "UNKNOWN_ERROR", "알 수 없는 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
