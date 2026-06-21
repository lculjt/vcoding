package com.vcoding.system.demo.api;

import com.vcoding.common.auth.AuthContext;
import com.vcoding.common.auth.CurrentUser;
import com.vcoding.common.response.ApiResponse;
import com.vcoding.system.demo.api.dto.DemoProfileResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoProfileController {

    /**
     * 受保护示例接口。用于验证 Gateway 鉴权和服务内部二次校验是否同时生效。
     */
    @GetMapping("/profile")
    public ApiResponse<DemoProfileResponse> profile() {
        CurrentUser currentUser = AuthContext.requireLogin();
        return ApiResponse.success(new DemoProfileResponse(
                "vcoding-system-demo",
                currentUser.userId(),
                currentUser.username(),
                currentUser.phone(),
                currentUser.adminFlag()
        ));
    }
}
