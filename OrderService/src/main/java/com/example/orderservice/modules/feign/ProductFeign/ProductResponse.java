package com.example.orderservice.modules.feign.ProductFeign;

public record ProductResponse(
        String name,
        String brand,
        String description,
        Integer price,
        String imageUrl,
        String videoUrl,
        String available,
        Integer quantity,
        Long categoryId
) {
}


