package com.demo.minimall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.minimall.dto.ProductCreateDTO;
import com.demo.minimall.entity.Product;
import com.demo.minimall.mapper.ProductMapper;
import com.demo.minimall.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }
    @Override
    public Page<Product> pageQuery(String keyword,int page,int pageSize){
        LambdaQueryWrapper<Product> wrapper =new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword),Product::getName,keyword)
                .orderByDesc(Product::getId);
        Page<Product> p=new Page<>(page,pageSize);
        return productMapper.selectPage(p,wrapper);
    }
    @Override
    public Product getById(Integer id){
        return productMapper.selectById(id);
    }
    @Override
    public void add(ProductCreateDTO dto){
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        productMapper.insert(product);
    }

}
