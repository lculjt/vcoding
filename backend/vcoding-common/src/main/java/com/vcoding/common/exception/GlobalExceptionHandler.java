package com.vcoding.common.exception;

import com.vcoding.common.response.ApiResponse;
import com.vcoding.common.response.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常保留自身 HTTP 状态码和错误码，便于前端按 code 做统一处理。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode, exception.getMessage(), exception.getData()));
    }

    /**
     * 处理 @Valid 请求体字段校验失败，并合并为前端可直接展示的错误文案。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return badRequest(message);
    }

    /**
     * 处理路径参数、查询参数等约束校验失败。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(
            ConstraintViolationException exception
    ) {
        return badRequest(exception.getMessage());
    }

    /**
     * 处理必填请求参数缺失。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception
    ) {
        return badRequest("缺少请求参数: " + exception.getParameterName());
    }

    /**
     * 处理 JSON 格式错误或请求体无法解析。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException() {
        return badRequest("请求体格式错误");
    }

    /**
     * 未预期异常统一收敛为系统异常，避免把服务端内部细节暴露给前端。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception exception) {
        log.error("未处理的系统异常", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ErrorCode.COMMON_INTERNAL_ERROR));
    }

    /**
     * 构造统一的 400 响应。
     */
    private ResponseEntity<ApiResponse<Object>> badRequest(String message) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(ErrorCode.COMMON_BAD_REQUEST, message));
    }

    /**
     * 格式化字段校验错误，保留字段名便于前端定位。
     */
    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
