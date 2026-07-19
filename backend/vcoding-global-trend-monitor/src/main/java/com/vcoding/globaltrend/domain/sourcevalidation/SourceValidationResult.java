package com.vcoding.globaltrend.domain.sourcevalidation;

import java.time.Instant;
import java.util.List;

public record SourceValidationResult(
        String sourceCode,
        boolean success,
        int statusCode,
        int itemCount,
        long durationMillis,
        String authentication,
        String rateLimit,
        List<String> observedFields,
        List<String> missingFields,
        String message,
        Instant validatedAt
) {
}
