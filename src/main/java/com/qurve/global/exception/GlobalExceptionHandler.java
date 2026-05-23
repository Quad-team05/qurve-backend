package com.qurve.global.exception;

import com.qurve.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 클래스
 *
 * * Controller 전역에서 발생하는 예외를 공통 형식으로 처리한다.
 *
 * * 예외 발생 시 일관된 API 응답 구조를 유지하기 위해
 * ApiResponse 기반 에러 응답을 반환한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     *
     * * 비즈니스 로직 수행 중 발생한 커스텀 예외를 처리한다.
     *
     * * ErrorCode 내부에 정의된 상태 코드와 메시지를 기반으로
     * 공통 에러 응답 형식을 반환한다.
     *
     * @param e 비즈니스 예외 객체
     * @return 공통 에러 응답 객체
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity
                .status(HttpStatus.valueOf(e.getErrorCode().getStatus()))
                .body(ApiResponse.error(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .get(0).getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }

}
