package com.berlin.aetherflow.system.user.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.system.user.domain.bo.UserManageUpdateBo;
import com.berlin.aetherflow.system.user.domain.query.UserManageQuery;
import com.berlin.aetherflow.system.user.domain.vo.UserManageVo;
import com.berlin.aetherflow.system.user.service.UserManageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端用户接口。
 */
@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserManageController {

    private final UserManageService userManageService;

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<PageResult<UserManageVo>> queryUserPage(@Validated UserManageQuery query) {
        return Result.success(userManageService.queryUserPage(query));
    }

    @Operation(summary = "管理端更新用户")
    @PutMapping("/{userId}")
    public Result<Void> updateUser(@PathVariable Long userId,
                                   @RequestBody @Valid UserManageUpdateBo bo,
                                   HttpServletRequest request) {
        userManageService.updateUser(userId, bo, request);
        return Result.success();
    }
}
