package com.vcoding.globaltrend.application.sourcevalidation;

import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import com.vcoding.globaltrend.domain.sourcevalidation.SourceValidationResult;
import com.vcoding.globaltrend.domain.sourcevalidation.SourceValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SourceValidationService {
    private final Map<String, SourceValidator> validators;

    public SourceValidationService(List<SourceValidator> sourceValidators) {
        this.validators = sourceValidators.stream()
                .collect(Collectors.toUnmodifiableMap(
                        validator -> validator.sourceCode().toLowerCase(Locale.ROOT),
                        Function.identity()
                ));
    }

    public SourceValidationResult validate(String sourceCode) {
        String normalizedCode = sourceCode.toLowerCase(Locale.ROOT);
        SourceValidator validator = validators.get(normalizedCode);
        if (validator == null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "暂不支持验证该数据源");
        }

        SourceValidationResult result = validator.validate();
        // 日志只保留状态、数量和耗时，避免把 API Key 或第三方响应写入日志。
        log.info(
                "数据源验证完成 sourceCode={} success={} statusCode={} itemCount={} durationMillis={}",
                result.sourceCode(),
                result.success(),
                result.statusCode(),
                result.itemCount(),
                result.durationMillis()
        );
        return result;
    }
}
