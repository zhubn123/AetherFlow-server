package com.berlin.aetherflow.system.user.service.impl;

import com.berlin.aetherflow.system.user.domain.entity.SysAuditLog;
import com.berlin.aetherflow.system.user.mapper.SysAuditLogMapper;
import com.berlin.aetherflow.system.user.service.SecurityAuditService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 安全审计服务实现。
 */
@Service
@AllArgsConstructor
public class SecurityAuditServiceImpl implements SecurityAuditService {

    private final SysAuditLogMapper sysAuditLogMapper;

    @Override
    public void record(Long userId, String username, String eventType, String eventName,
                       String requestUri, String clientIp, Integer result, String message) {
        SysAuditLog log = new SysAuditLog();
        log.setUserId(userId);
        log.setUsername(limitLength(trimToNull(username), 64));
        log.setEventType(limitLength(trimToNull(eventType), 32));
        log.setEventName(limitLength(trimToNull(eventName), 64));
        log.setRequestUri(limitLength(trimToNull(requestUri), 255));
        log.setClientIp(limitLength(trimToNull(clientIp), 64));
        log.setResult(result);
        log.setMessage(limitLength(trimToNull(message), 255));
        log.setOccurTime(LocalDateTime.now());
        sysAuditLogMapper.insert(log);
    }

    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String limitLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
