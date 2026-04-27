package com.berlin.aetherflow.system.user.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.berlin.aetherflow.config.ServletUtils;
import com.berlin.aetherflow.exception.ApiException;
import com.berlin.aetherflow.system.user.domain.bo.UserPasswordUpdateBo;
import com.berlin.aetherflow.system.user.domain.bo.UserProfileUpdateBo;
import com.berlin.aetherflow.system.user.domain.entity.SysUser;
import com.berlin.aetherflow.system.user.domain.vo.UserProfileVo;
import com.berlin.aetherflow.system.user.mapper.SysUserMapper;
import com.berlin.aetherflow.system.user.service.SecurityAuditService;
import com.berlin.aetherflow.system.user.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 用户个人资料服务实现。
 */
@Service
@AllArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private static final int USER_STATUS_NORMAL = 0;

    private final SysUserMapper sysUserMapper;
    private final SecurityAuditService securityAuditService;

    @Override
    public UserProfileVo getCurrentProfile() {
        SysUser current = getCurrentUser();
        UserProfileVo vo = new UserProfileVo();
        vo.setId(current.getId());
        vo.setUsername(current.getUsername());
        vo.setNickname(current.getNickname());
        vo.setEmail(current.getEmail());
        vo.setPhone(current.getPhone());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentProfile(UserProfileUpdateBo bo, HttpServletRequest request) {
        SysUser current = getCurrentUser();
        String requestUri = request == null ? null : request.getRequestURI();
        String clientIp = request == null ? null : ServletUtils.getClientIpAddress(request);

        try {
            String username = normalizeRequired(bo == null ? null : bo.getUsername(), "用户名不能为空");
            String email = normalizeOptional(bo == null ? null : bo.getEmail());
            String nickname = normalizeOptional(bo == null ? null : bo.getNickname());
            String phone = normalizeOptional(bo == null ? null : bo.getPhone());

            if (!Objects.equals(username, current.getUsername())) {
                SysUser duplicate = sysUserMapper.selectByColumn(SysUser::getUsername, username);
                if (duplicate != null && !Objects.equals(duplicate.getId(), current.getId())) {
                    throw ApiException.business("用户名已存在");
                }
            }
            if (StringUtils.isNotBlank(email)) {
                SysUser duplicateEmail = sysUserMapper.selectByColumn(SysUser::getEmail, email);
                if (duplicateEmail != null && !Objects.equals(duplicateEmail.getId(), current.getId())) {
                    throw ApiException.business("邮箱已被占用");
                }
            }

            SysUser toUpdate = new SysUser();
            toUpdate.setId(current.getId());
            toUpdate.setUsername(username);
            toUpdate.setNickname(StringUtils.defaultIfBlank(nickname, username));
            toUpdate.setEmail(email);
            toUpdate.setPhone(phone);
            sysUserMapper.updateById(toUpdate);

            securityAuditService.record(
                    current.getId(),
                    current.getUsername(),
                    "PROFILE",
                    "UPDATE_PROFILE",
                    requestUri,
                    clientIp,
                    1,
                    "资料更新成功"
            );
        } catch (RuntimeException ex) {
            securityAuditService.record(
                    current.getId(),
                    current.getUsername(),
                    "PROFILE",
                    "UPDATE_PROFILE",
                    requestUri,
                    clientIp,
                    0,
                    ex.getMessage()
            );
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentPassword(UserPasswordUpdateBo bo, HttpServletRequest request) {
        SysUser current = getCurrentUser();
        String requestUri = request == null ? null : request.getRequestURI();
        String clientIp = request == null ? null : ServletUtils.getClientIpAddress(request);

        try {
            String oldPassword = normalizeRequired(bo == null ? null : bo.getOldPassword(), "旧密码不能为空");
            String newPassword = normalizeRequired(bo == null ? null : bo.getNewPassword(), "新密码不能为空");
            if (Objects.equals(oldPassword, newPassword)) {
                throw ApiException.badRequest("新密码不能与旧密码相同");
            }
            validatePasswordStrength(newPassword);

            if (!BCrypt.checkpw(oldPassword, current.getPasswordHash())) {
                throw ApiException.unauthorized("旧密码不正确");
            }

            SysUser toUpdate = new SysUser();
            toUpdate.setId(current.getId());
            toUpdate.setPasswordHash(BCrypt.hashpw(newPassword));
            toUpdate.setLoginFailCount(0);
            toUpdate.setLockUntil(null);
            toUpdate.setStatus(USER_STATUS_NORMAL);
            sysUserMapper.updateById(toUpdate);

            securityAuditService.record(
                    current.getId(),
                    current.getUsername(),
                    "PASSWORD",
                    "UPDATE_PASSWORD",
                    requestUri,
                    clientIp,
                    1,
                    "密码修改成功"
            );
        } catch (RuntimeException ex) {
            securityAuditService.record(
                    current.getId(),
                    current.getUsername(),
                    "PASSWORD",
                    "UPDATE_PASSWORD",
                    requestUri,
                    clientIp,
                    0,
                    ex.getMessage()
            );
            throw ex;
        }
    }

    private SysUser getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw ApiException.unauthorized("登录状态无效，请重新登录");
        }
        return user;
    }

    private String normalizeRequired(String input, String message) {
        String normalized = normalizeOptional(input);
        if (StringUtils.isBlank(normalized)) {
            throw ApiException.badRequest(message);
        }
        return normalized;
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }

    private void validatePasswordStrength(String newPassword) {
        if (newPassword.length() < 8) {
            throw ApiException.badRequest("新密码长度不能小于8位");
        }
        boolean hasLetter = newPassword.chars().anyMatch(Character::isLetter);
        boolean hasDigit = newPassword.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw ApiException.badRequest("新密码必须包含字母和数字");
        }
    }
}
