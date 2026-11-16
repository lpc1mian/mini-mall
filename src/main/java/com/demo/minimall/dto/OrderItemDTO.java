package com.demo.minimall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemDTO {
    @NotNull(message="商品ID不能为空")
    private Integer productId;
    @NotNull(message="数量不能为空")
    @Min(value = 1, message="数量必须至少为1")
    private Integer quantity;
}
