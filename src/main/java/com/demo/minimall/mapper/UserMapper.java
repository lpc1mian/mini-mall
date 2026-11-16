package com.demo.minimall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.minimall.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
