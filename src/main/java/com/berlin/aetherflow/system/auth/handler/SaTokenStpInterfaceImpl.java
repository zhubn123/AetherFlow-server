package com.berlin.aetherflow.system.auth.handler;

import cn.dev33.satoken.stp.StpInterface;
import com.berlin.aetherflow.system.user.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 角色提供器。
 */
@Component
@AllArgsConstructor
public class SaTokenStpInterfaceImpl implements StpInterface {

    private final AuthService authService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 阶段一先不启用权限码，后续扩展为菜单/按钮级权限。
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = parseUserId(loginId);
        if (userId == null) {
            return List.of();
        }
        return authService.getRoleKeysByUserId(userId);
    }

    private Long parseUserId(Object loginId) {
        if (loginId == null) {
            return null;
        }
        if (loginId instanceof Long id) {
            return id;
        }
        if (loginId instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(loginId));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
