package com.berlin.aetherflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.domain.entity.User;
import com.berlin.aetherflow.mapper.UserMapper;
import com.berlin.aetherflow.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
