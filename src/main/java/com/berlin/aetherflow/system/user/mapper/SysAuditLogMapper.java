package com.berlin.aetherflow.system.user.mapper;

import com.berlin.aetherflow.common.BaseMapperPlus;
import com.berlin.aetherflow.system.user.domain.entity.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 安全审计日志 Mapper。
 */
@Mapper
public interface SysAuditLogMapper extends BaseMapperPlus<SysAuditLog> {
}
