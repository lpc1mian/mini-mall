package com.demo.minimall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.minimall.common.BusinessException;
import com.demo.minimall.dto.OrderCreateDTO;
import com.demo.minimall.dto.OrderItemDTO;
import com.demo.minimall.entity.Order;
import com.demo.minimall.entity.OrderItem;
import com.demo.minimall.entity.Product;
import com.demo.minimall.mapper.OrderItemMapper;
import com.demo.minimall.mapper.OrderMapper;
import com.demo.minimall.mapper.ProductMapper;
import com.demo.minimall.service.OrderService;
import com.demo.minimall.vo.OrderDetailVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(OrderCreateDTO dto, Integer userId) {
        Map<Integer, Integer> merged = new LinkedHashMap<>();
        for (OrderItemDTO item : dto.getItems()) {
            merged.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, Product> productMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : merged.entrySet()) {
            Integer productId = entry.getKey();
            Integer qty = entry.getValue();
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new BusinessException("商品不存在：" + productId);
            }
            if (product.getStock() < qty) {
                throw new BusinessException("库存不足：" + product.getName());
            }
            productMap.put(productId, product);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setTotalAmount(total);
        order.setStatus(0);
        orderMapper.insert(order);
        for (Map.Entry<Integer, Integer> entry : merged.entrySet()) {
            Integer productId = entry.getKey();
            Integer qty = entry.getValue();
            Product product = productMap.get(productId);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(productId);
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(qty);
            orderItemMapper.insert(orderItem);
            int rows = productMapper.decreaseStock(productId, qty);
            if (rows == 0) {
                throw new BusinessException("库存不足：" + product.getName());
            }
        }
        return order.getOrderNo();
    }

    @Override
    public Page<Order> pageMyOrders(Integer userId, int page, int pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
                .orderByDesc(Order::getId);
        return orderMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public OrderDetailVO getDetail(Integer orderId, Integer userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看");
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrder(order);
        vo.setItems(items);
        return vo;
    }

    private String generateOrderNo() {
        return System.currentTimeMillis() + String.valueOf((int) (Math.random() * 9000 + 1000));
    }
}
