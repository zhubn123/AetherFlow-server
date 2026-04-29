package com.berlin.aetherflow.system.user.controller;

import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.system.user.domain.bo.RoleManageSaveBo;
import com.berlin.aetherflow.system.user.domain.query.RoleManageQuery;
import com.berlin.aetherflow.system.user.domain.vo.RoleManageVo;
import com.berlin.aetherflow.system.user.domain.vo.RoleOptionVo;
import com.berlin.aetherflow.system.user.service.RoleManageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端角色接口。
 */
@RestController
@RequestMapping("/api/roles")
@AllArgsConstructor
public class RoleManageController {

    private final RoleManageService roleManageService;

    @Operation(summary = "分页查询角色")
    @GetMapping
    public Result<PageResult<RoleManageVo>> queryRolePage(@Validated RoleManageQuery query) {
        return Result.success(roleManageService.queryRolePage(query));
    }

    @Operation(summary = "查询角色详情")
    @GetMapping("/{roleId}")
    public Result<RoleManageVo> getRoleDetail(@PathVariable Long roleId) {
        return Result.success(roleManageService.getRoleDetail(roleId));
    }

    @Operation(summary = "查询角色选项")
    @GetMapping("/options")
    public Result<List<RoleOptionVo>> listRoleOptions() {
        return Result.success(roleManageService.listRoleOptions());
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<Void> createRole(@RequestBody @Valid RoleManageSaveBo bo) {
        roleManageService.createRole(bo);
        return Result.success();
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{roleId}")
    public Result<Void> updateRole(@PathVariable Long roleId, @RequestBody @Valid RoleManageSaveBo bo) {
        roleManageService.updateRole(roleId, bo);
        return Result.success();
    }

    @Operation(summary = "批量删除角色")
    @DeleteMapping
    public Result<Void> deleteRoles(@RequestParam List<Long> ids) {
        roleManageService.deleteRoles(ids);
        return Result.success();
    }
}
