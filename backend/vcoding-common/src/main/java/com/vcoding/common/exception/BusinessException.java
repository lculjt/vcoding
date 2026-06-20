package com.vcoding.common.exception;

import com.vcoding.common.response.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object data;

    /**
     * 使用错误码默认文案抛出业务异常。
     */
    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage(), null);
    }

    /**
     * 使用自定义文案抛出业务异常。
     */
    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    /**
     * 携带扩展数据抛出业务异常，适合返回字段级错误、补充上下文等场景。
     */
    public BusinessException(ErrorCode errorCode, String message, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }
}
