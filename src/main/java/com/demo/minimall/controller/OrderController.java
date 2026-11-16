package com.demo.minimall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.minimall.common.Result;
import com.demo.minimall.dto.OrderCreateDTO;
import com.demo.minimall.entity.Order;
import com.demo.minimall.entity.User;
import com.demo.minimall.service.OrderService;
import com.demo.minimall.vo.OrderDetailVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<String> createOrder(@RequestBody @Valid OrderCreateDTO dto,
                                      @RequestAttribute("currentUser") User user) {
        String orderNo = orderService.createOrder(dto, user.getId());
        return Result.success(orderNo);
    }
    @GetMapping("/my")
    public Result<Page<Order>> my(
            @RequestAttribute("currentUser") User user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize
    ){
        return Result.success(orderService.pageMyOrders(user.getId(),page,pageSize));
    }
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Integer id,
                                        @RequestAttribute("currentUser") User user){
        return Result.success(orderService.getDetail(id, user.getId()));
    }
}
