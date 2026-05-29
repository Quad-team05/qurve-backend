package com.qurve.global.common;

import com.qurve.global.enums.ErrorCode;
import lombok.*;

/**
 * 공통 API 응답 객체
 *
 * * 모든 API가 통일된 형식의 응답을 반환하기 위한 규격을 정의한다.
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApiResponse<T> {
    // 요청 성공 여부
    private boolean success;
    // 사용자에게 전달할 응답 메시지
    private String message;
    // 성공 시 반환되는 실제 데이터
    private T data;
    // 비즈니스 응답 코드 (성공 시 "SUCCESS", 실패 시 에러 코드)
    private String code;


    /**
     * 성공 응답 생성
     *
     * * 정상 요청 처리 시 공통 성공 응답 포맷을 반환한다.
     *
     * @param data 응답 데이터
     * @return 성공 응답 객체
     * @param <T> 응답 데이터 타입
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message("요청 성공")
                .data(data)
                .build();
    }

    /**
     * 에러 응답 생성(String 기반)
     *
     * * 동적으로 메시지가 바뀌는 경우를 처리한다.
     *
     * @param code 비즈니스 에러 코드
     * @param message 사용자 응답 메시지
     * @return 실패 응답 객체
     */
    public static ApiResponse<Void> error(String code, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 에러 응답 생성(ErrorCode 기반)
     *
     * * 미리 정의된 에러를 ErrorCode 정보를 기반으로 처리한다.
     *
     * @param errorCode 공통 에러 코드 객체
     * @return 실패 응답 객체
     */
    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }
}
