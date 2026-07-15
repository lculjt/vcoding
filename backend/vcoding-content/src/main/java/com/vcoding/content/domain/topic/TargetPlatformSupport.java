package com.vcoding.content.domain.topic;

import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 目标平台多选值与数据库存储之间的转换工具。数据库仍使用逗号分隔字符串，应用层统一按枚举集合处理。
 */
public final class TargetPlatformSupport {
    private static final String DELIMITER = ",";
    private static final int MAX_STORAGE_LENGTH = 255;

    private TargetPlatformSupport() {
    }

    /**
     * 校验并解析请求中的平台编码列表，去重后保持前端传入顺序。
     */
    public static List<TargetPlatform> parseRequestCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> uniqueCodes = new LinkedHashSet<>();
        for (String code : codes) {
            if (StringUtils.hasText(code)) {
                uniqueCodes.add(code.trim());
            }
        }

        List<TargetPlatform> platforms = new ArrayList<>(uniqueCodes.size());
        for (String code : uniqueCodes) {
            TargetPlatform platform = TargetPlatform.fromCode(code);
            if (platform == null) {
                throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "不支持的目标平台: " + code);
            }
            platforms.add(platform);
        }
        return platforms;
    }

    /**
     * 将平台枚举集合序列化为数据库存储值。
     */
    public static String toStorageValue(List<TargetPlatform> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            return null;
        }

        String storageValue = platforms.stream()
                .map(TargetPlatform::getCode)
                .collect(Collectors.joining(DELIMITER));
        if (storageValue.length() > MAX_STORAGE_LENGTH) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "目标平台数量过多");
        }
        return storageValue;
    }

    /**
     * 将数据库存储值还原为平台编码列表，忽略无法识别的历史脏数据。
     */
    public static List<String> toCodeList(String storageValue) {
        if (!StringUtils.hasText(storageValue)) {
            return List.of();
        }

        LinkedHashSet<String> codes = new LinkedHashSet<>();
        Arrays.stream(storageValue.split(DELIMITER))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(code -> {
                    TargetPlatform platform = TargetPlatform.fromCode(code);
                    if (platform != null) {
                        codes.add(platform.getCode());
                    }
                });
        return List.copyOf(codes);
    }
}
