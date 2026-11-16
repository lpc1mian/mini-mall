package com.demo.minimall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("mall_order")
@Data
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String orderNo;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createTime;

}
