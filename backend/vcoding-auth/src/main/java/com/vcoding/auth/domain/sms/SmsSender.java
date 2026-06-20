package com.vcoding.auth.domain.sms;

public interface SmsSender {
    void sendSmsCode(String phone, SmsScene scene, String code);
}
