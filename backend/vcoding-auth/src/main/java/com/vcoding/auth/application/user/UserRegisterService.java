package com.vcoding.auth.application.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vcoding.auth.api.dto.RegisterRequest;
import com.vcoding.auth.api.dto.RegisterResponse;
import com.vcoding.auth.application.captcha.SmsCodeService;
import com.vcoding.auth.application.crypto.PasswordCryptoService;
import com.vcoding.auth.domain.sms.SmsScene;
import com.vcoding.auth.domain.user.UserStatus;
import com.vcoding.auth.infrastructure.persistence.entity.UserEntity;
import com.vcoding.auth.infrastructure.persistence.mapper.UserMapper;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRegisterService {
    private final UserMapper userMapper;
    private final PasswordHashService passwordHashService;
    private final SmsCodeService smsCodeService;
    private final PasswordCryptoService passwordCryptoService;

    /**
     * 开放用户自助注册。注册前先校验短信验证码，再检查账号唯一性并保存密码哈希。
     */
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String phone = request.getPhone().trim();

        smsCodeService.verifyAndDelete(SmsScene.REGISTER, phone, request.getSmsCode());
        ensureUsernameAvailable(username);
        ensurePhoneAvailable(phone);

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPhone(phone);
        String rawPassword = passwordCryptoService.decrypt(request.getPasswordCiphertext(), request.getPasswordKeyId());
        user.setPasswordHash(passwordHashService.hash(rawPassword));
        user.setStatus(UserStatus.ENABLED.getCode());
        user.setAdminFlag(false);
        userMapper.insert(user);

        return new RegisterResponse(user.getId(), user.getUsername(), user.getPhone(), user.getAdminFlag());
    }

    private void ensureUsernameAvailable(String username) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username));
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.AUTH_USERNAME_ALREADY_EXISTS);
        }
    }

    private void ensurePhoneAvailable(String phone) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getPhone, phone));
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.AUTH_PHONE_ALREADY_EXISTS);
        }
    }
}
