package com.vcoding.common.response;

import java.util.List;

/**
 * 通用分页响应。业务模块只负责构造 records，分页元信息统一使用该结构返回。
 */
public record PageResponse<T>(
        long pageNo,
        long pageSize,
        long total,
        long pages,
        List<T> records
) {
}
