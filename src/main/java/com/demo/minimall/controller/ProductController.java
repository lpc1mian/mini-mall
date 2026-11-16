package com.demo.minimall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.minimall.common.Result;
import com.demo.minimall.dto.ProductCreateDTO;
import com.demo.minimall.entity.Product;
import com.demo.minimall.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping
    public Result<Page<Product>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize)
    {
        return  Result.success(productService.pageQuery(keyword,page,pageSize));
    }
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Integer id){
        Product product = productService.getById(id);
        return Result.success(product);
    }
    @PostMapping
    public Result<String> add(@RequestBody @Valid ProductCreateDTO dto){
        productService.add(dto);
        return Result.success("添加成功");
    }


}
