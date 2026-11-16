package com.demo.minimall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.minimall.dto.ProductCreateDTO;
import com.demo.minimall.entity.Product;

public interface ProductService {
    Page<Product> pageQuery(String keyword, int page, int pageSize);
    Product getById(Integer id);
    void add(ProductCreateDTO dto);
}
