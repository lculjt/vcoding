package com.vcoding.globaltrend.domain.sourcevalidation;

public interface SourceValidator {
    String sourceCode();

    SourceValidationResult validate();
}
