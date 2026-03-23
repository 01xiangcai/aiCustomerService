package com.aics.server.controller;

import com.aics.common.result.R;
import com.aics.server.dto.request.LoginRequest;
import com.aics.server.dto.request.RegisterRequest;
import com.aics.server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 租户注册
     */
    @PostMapping("/register")
    public R<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> result = authService.register(
                request.getTenantName(),
                request.getEmail(),
                request.getPassword());
        return R.ok(result);
    }

    /**
     * 租户登录
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> result = authService.login(
                request.getEmail(),
                request.getPassword());
        return R.ok(result);
    }
}
