package com.example.orderservice.modules.feign.ProductFeign;

public record ProductResponse(
    String name,
    String description,
    Integer price,
    String imageUrl,
    String available,
    Integer quantity,
    Long categoryId
) {
}


