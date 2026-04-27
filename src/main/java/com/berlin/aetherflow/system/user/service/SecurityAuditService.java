package com.berlin.aetherflow.system.user.service;

/**
 * 安全审计服务。
 */
public interface SecurityAuditService {

    /**
     * 记录安全审计事件。
     *
     * @param userId    用户 ID（可空）
     * @param username  用户名（可空）
     * @param eventType 事件类型（LOGIN/PROFILE/PASSWORD）
     * @param eventName 事件名称
     * @param requestUri 请求路径
     * @param clientIp  客户端 IP
     * @param result    执行结果（1成功 0失败）
     * @param message   结果消息
     */
    void record(Long userId, String username, String eventType, String eventName,
                String requestUri, String clientIp, Integer result, String message);
}
