package com.demo.minimall.vo;

import com.demo.minimall.entity.Order;
import com.demo.minimall.entity.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailVO {
    private Order order;
    private List<OrderItem> items;
}
