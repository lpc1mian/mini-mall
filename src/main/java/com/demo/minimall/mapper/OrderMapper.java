package com.demo.minimall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.minimall.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
