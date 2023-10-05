package com.example.online_farm.Controller;

import com.example.online_farm.DTO.ProductsLimit;
import com.example.online_farm.Service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ProductsLimit getProducts(@RequestParam(defaultValue = "2") int page,
                                     @RequestParam(defaultValue = "2") int limit) {
        return productService.getAllProducts(page, limit);
    }
}
