package com.vcoding.globaltrend.infrastructure.external.validation;

import com.vcoding.globaltrend.domain.sourcevalidation.SourceValidationResult;

import java.time.Instant;
import java.util.List;

public final class ValidationResultFactory {
    private ValidationResultFactory() {
    }

    public static SourceValidationResult success(
            String sourceCode,
            int statusCode,
            int itemCount,
            long durationMillis,
            String authentication,
            String rateLimit,
            List<String> observedFields,
            List<String> missingFields,
            String message
    ) {
        return new SourceValidationResult(
                sourceCode,
                true,
                statusCode,
                itemCount,
                durationMillis,
                authentication,
                rateLimit,
                List.copyOf(observedFields),
                List.copyOf(missingFields),
                message,
                Instant.now()
        );
    }

    public static SourceValidationResult failure(
            String sourceCode,
            int statusCode,
            long durationMillis,
            String authentication,
            String rateLimit,
            String message
    ) {
        return new SourceValidationResult(
                sourceCode,
                false,
                statusCode,
                0,
                durationMillis,
                authentication,
                rateLimit,
                List.of(),
                List.of(),
                message,
                Instant.now()
        );
    }
}
