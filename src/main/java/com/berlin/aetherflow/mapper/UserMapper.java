package com.berlin.aetherflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
