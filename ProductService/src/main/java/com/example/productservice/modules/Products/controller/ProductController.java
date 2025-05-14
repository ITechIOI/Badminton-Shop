package com.example.productservice.modules.Products.controller;

import com.example.productservice.models.Products;
import com.example.productservice.modules.Products.dto.CreateProductDto;
import com.example.productservice.modules.Products.dto.ProductResponse;
import com.example.productservice.modules.Products.dto.UpdateProductDto;
import com.example.productservice.modules.Products.service.CloudinaryService;
import com.example.productservice.modules.Products.service.ProductService;
import com.example.productservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequestMapping("/products/products")
@RestController
@AllArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final CloudinaryService cloudinaryService;

    @PostMapping("/new")
    public ResponseEntity<Products> createProduct(
            @ModelAttribute CreateProductDto createProductDto
    ) {
        return ResponseEntity.ok(productService.createProduct(createProductDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Products> updateProduct(
            @PathVariable("id") Long id,
            @ModelAttribute UpdateProductDto createProductDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        Products product = productService.findProductById(id);
        return ResponseEntity.ok(productService.updateProduct(id, createProductDto));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Products> getProductById(@PathVariable("id") Long id) {
        System.out.println("ProductController.getProductById");
        return ResponseEntity.ok(productService.findProductById(id));
    }

    @GetMapping("/service/{id}")
    public ResponseEntity<ProductResponse> getProductByIdForServices(@PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.findProductByIdForService(id));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<Products>> getAllProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(productService.getAllProducts(page, limit));
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<PagedResponse<Products>> getProductsByBrand(
            @PathVariable("brand") String brand,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(productService.getProductsByBrand(brand, page, limit));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<PagedResponse<Products>> getProductsByCategory(
            @PathVariable("category") Long category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(productService.getProductsByCategoryId(category, page, limit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(null);
    }
}
