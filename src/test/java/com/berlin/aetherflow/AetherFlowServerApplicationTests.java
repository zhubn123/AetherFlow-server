package com.berlin.aetherflow;

import com.berlin.aetherflow.system.user.domain.entity.User;
import com.berlin.aetherflow.system.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AetherFlowServerApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {

    }



}
