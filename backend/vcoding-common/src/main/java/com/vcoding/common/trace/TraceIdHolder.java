package com.vcoding.common.trace;

import org.slf4j.MDC;

public final class TraceIdHolder {
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    private TraceIdHolder() {
    }

    /**
     * 将 traceId 放入 MDC，后续日志格式可以直接读取 traceId 字段。
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_MDC_KEY, traceId);
    }

    /**
     * 获取当前线程绑定的 traceId，用于统一响应体。
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_MDC_KEY);
    }

    /**
     * 清理当前线程绑定的 traceId。
     */
    public static void clear() {
        MDC.remove(TRACE_ID_MDC_KEY);
    }
}
