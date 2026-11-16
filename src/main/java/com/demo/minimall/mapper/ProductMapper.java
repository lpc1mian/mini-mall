package com.demo.minimall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.minimall.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    @Update("UPDATE mall_product SET stock = stock - #{qty} WHERE id = #{productId} AND stock >= #{qty}")
    int decreaseStock(@Param("productId") Integer productId, @Param("qty") Integer qty);
}
