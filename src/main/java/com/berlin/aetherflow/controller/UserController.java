package com.berlin.aetherflow.controller;

import com.berlin.aetherflow.service.UserService;
import com.berlin.aetherflow.domain.entity.User;
import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.exception.ResultCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<List<User>> listUsers() {
        return Result.success(userService.list());
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return Result.success(user);
    }
}
