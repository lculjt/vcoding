package com.vcoding.common.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将已经通过 Gateway 二次校验的当前登录用户注入到 Controller 方法参数。
 * <p>
 * 该注解只负责读取 {@link AuthContext} 中的用户，不承担鉴权职责。
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentLoginUser {
}
