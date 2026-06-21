package com.vcoding.common.auth;

import com.vcoding.common.auth.config.GatewayInternalProperties;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GatewayUserHeaderService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final GatewayInternalProperties gatewayProperties;

    /**
     * Gateway 转发前生成内部用户头。签名覆盖用户信息和时间戳，防止用户头被外部伪造。
     */
    public Map<String, String> buildSignedHeaders(CurrentUser currentUser) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(AuthHeaderNames.USER_ID, String.valueOf(currentUser.userId()));
        headers.put(AuthHeaderNames.USERNAME, currentUser.username());
        headers.put(AuthHeaderNames.PHONE, currentUser.phone());
        headers.put(AuthHeaderNames.ADMIN, String.valueOf(currentUser.adminFlag()));
        headers.put(AuthHeaderNames.GATEWAY_TIMESTAMP, timestamp);
        headers.put(AuthHeaderNames.GATEWAY_SIGNATURE, sign(currentUser, timestamp));
        return headers;
    }

    /**
     * 内部服务读取并校验 Gateway 用户头，校验通过后才能信任其中的用户身份。
     */
    public CurrentUser verifyAndParse(Map<String, String> headers) {
        String userId = headers.get(AuthHeaderNames.USER_ID);
        String username = headers.get(AuthHeaderNames.USERNAME);
        String phone = headers.get(AuthHeaderNames.PHONE);
        String admin = headers.get(AuthHeaderNames.ADMIN);
        String timestamp = headers.get(AuthHeaderNames.GATEWAY_TIMESTAMP);
        String signature = headers.get(AuthHeaderNames.GATEWAY_SIGNATURE);

        if (!StringUtils.hasText(userId)
                || !StringUtils.hasText(username)
                || !StringUtils.hasText(phone)
                || !StringUtils.hasText(admin)
                || !StringUtils.hasText(timestamp)
                || !StringUtils.hasText(signature)) {
            throw new BusinessException(ErrorCode.AUTH_NOT_LOGIN);
        }

        long timestampSeconds = parseTimestamp(timestamp);
        long nowSeconds = Instant.now().getEpochSecond();
        if (Math.abs(nowSeconds - timestampSeconds) > gatewayProperties.getInternalTimestampTtlSeconds()) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        CurrentUser currentUser = new CurrentUser(parseUserId(userId), username, phone, Boolean.parseBoolean(admin));
        String expectedSignature = sign(currentUser, timestamp);
        if (!expectedSignature.equals(signature)) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
        return currentUser;
    }

    private long parseTimestamp(String timestamp) {
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    private Long parseUserId(String userId) {
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    private String sign(CurrentUser currentUser, String timestamp) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    gatewayProperties.getInternalSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            ));
            byte[] signature = mac.doFinal(payload(currentUser, timestamp).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    private String payload(CurrentUser currentUser, String timestamp) {
        return currentUser.userId()
                + "\n" + currentUser.username()
                + "\n" + currentUser.phone()
                + "\n" + currentUser.adminFlag()
                + "\n" + timestamp;
    }
}
