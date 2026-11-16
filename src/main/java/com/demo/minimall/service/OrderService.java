package com.demo.minimall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.minimall.dto.OrderCreateDTO;
import com.demo.minimall.entity.Order;
import com.demo.minimall.vo.OrderDetailVO;

public interface OrderService {
    String createOrder(OrderCreateDTO dto, Integer userId);
    Page<Order> pageMyOrders(Integer userId,int page,int pageSize);
    OrderDetailVO getDetail(Integer orderId,Integer userId);
}
