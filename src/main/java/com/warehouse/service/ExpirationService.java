package com.warehouse.service;

import com.warehouse.model.Product;

import java.util.List;

public class ExpirationService {

    private final ProductService productService = new ProductService();

    public List<Product> getExpiringSoon() {
        return productService.expiringWithinDays(7);
    }
}