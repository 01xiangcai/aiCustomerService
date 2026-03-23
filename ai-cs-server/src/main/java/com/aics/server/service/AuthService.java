package com.aics.server.service;

import cn.hutool.core.util.RandomUtil;
import com.aics.common.entity.Tenant;
import com.aics.common.exception.BizException;
import com.aics.server.mapper.TenantMapper;
import com.aics.server.security.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务：注册、登录
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantMapper tenantMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * 租户注册
     *
     * @param tenantName 租户名
     * @param email      邮箱
     * @param password   密码
     * @return 注册结果（含 Token）
     */
    public Map<String, Object> register(String tenantName, String email, String password) {
        // 校验邮箱是否已注册
        Long count = tenantMapper.selectCount(
                new LambdaQueryWrapper<Tenant>().eq(Tenant::getEmail, email));
        if (count > 0) {
            throw BizException.badRequest("该邮箱已注册");
        }

        // 创建租户
        Tenant tenant = new Tenant();
        tenant.setTenantName(tenantName);
        tenant.setEmail(email);
        tenant.setPassword(passwordEncoder.encode(password));
        tenant.setStatus(1);
        tenantMapper.insert(tenant);

        // 生成 Token
        String token = jwtUtils.generateToken(tenant.getId(), email);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tenantId", tenant.getId());
        result.put("tenantName", tenantName);
        return result;
    }

    /**
     * 租户登录
     *
     * @param email    邮箱
     * @param password 密码
     * @return 登录结果（含 Token）
     */
    public Map<String, Object> login(String email, String password) {
        Tenant tenant = tenantMapper.selectOne(
                new LambdaQueryWrapper<Tenant>().eq(Tenant::getEmail, email));
        if (tenant == null) {
            throw BizException.badRequest("邮箱或密码错误");
        }
        if (!passwordEncoder.matches(password, tenant.getPassword())) {
            throw BizException.badRequest("邮箱或密码错误");
        }
        if (tenant.getStatus() != 1) {
            throw BizException.forbidden("账号已被禁用");
        }

        String token = jwtUtils.generateToken(tenant.getId(), email);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tenantId", tenant.getId());
        result.put("tenantName", tenant.getTenantName());
        return result;
    }
}
