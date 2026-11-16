package com.demo.minimall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.minimall.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
