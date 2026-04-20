package com.berlin.aetherflow.system.user.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.exception.ResultCode;
import com.berlin.aetherflow.system.user.domain.bo.LoginUser;
import com.berlin.aetherflow.system.user.domain.entity.User;
import com.berlin.aetherflow.system.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<List<User>> listUsers() {
        return Result.success(userService.list());
    }

    // @PostMapping("login")
    // public Result<User> login(@RequestParam String username,@RequestParam String password) {
    //     if (username.equals("admin") && password.equals("123456")){
    //         return Result.success();
    //     }
    //     return Result.fail("登录失败");
    // }

    @Operation(summary = "登录")
    @PostMapping("login")
    public Result<String> login(@RequestBody LoginUser user, HttpServletRequest request) {
        String token = userService.login(user,request);
        return Result.success(token);
    }
}
