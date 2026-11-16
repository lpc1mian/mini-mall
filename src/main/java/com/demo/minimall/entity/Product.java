package com.demo.minimall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("mall_product")
@Data
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private LocalDateTime createTime;
}
