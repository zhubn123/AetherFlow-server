package com.berlin.aetherflow.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.common.BaseMapperPlus;
import com.berlin.aetherflow.system.user.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapperPlus<User> {
}
