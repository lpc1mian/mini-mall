package com.demo.minimall.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {
    @NotEmpty(message = "订单不能为空")
    @Valid
    private List<OrderItemDTO> items;
}
