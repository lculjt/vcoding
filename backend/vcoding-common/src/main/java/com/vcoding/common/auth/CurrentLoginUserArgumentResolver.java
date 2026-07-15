package com.vcoding.common.auth;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentLoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 只解析标注了 @CurrentLoginUser 且类型为 CurrentUser 的参数，避免误伤普通参数。
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentLoginUser.class)
                && CurrentUser.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 从 AuthContext 读取当前用户。若前置 Gateway 二次校验没有通过，这里会返回未登录错误。
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        return AuthContext.requireLogin();
    }
}
