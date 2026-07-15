package com.vcoding.common.response;

import com.vcoding.common.trace.TraceIdHolder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private static final int SUCCESS_CODE = 0;

    private final int code;
    private final String message;
    private final T data;
    private final String traceId;

    /**
     * 构造成功响应，并自动携带当前请求的 traceId。
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                SUCCESS_CODE,
                ErrorCode.SUCCESS.getMessage(),
                data,
                TraceIdHolder.getTraceId()
        );
    }

    /**
     * 无响应体的成功结果。
     */
    public static ApiResponse<Void> success() {
        return success(null);
    }

    /**
     * 使用错误码默认文案构造失败响应。
     */
    public static ApiResponse<Object> fail(ErrorCode errorCode) {
        return fail(errorCode, errorCode.getMessage(), null);
    }

    /**
     * 使用自定义文案构造失败响应。
     */
    public static ApiResponse<Object> fail(ErrorCode errorCode, String message) {
        return fail(errorCode, message, null);
    }

    /**
     * 构造带扩展数据的失败响应，并自动携带当前请求的 traceId。
     */
    public static ApiResponse<Object> fail(ErrorCode errorCode, String message, Object data) {
        return new ApiResponse<>(
                errorCode.getHttpStatus(),
                message,
                data,
                TraceIdHolder.getTraceId()
        );
    }
}
