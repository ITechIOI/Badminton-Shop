package com.example.notificationservice.modules.feign.Products;

public record ProductResponse(
        String name,
        String description,
        Integer price,
        String imageUrl,
        String available,
        Integer quantity,
        Long categoryId
) {
    public String toString() {
        return "ProductResponse{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", available='" + available + '\'' +
                ", quantity=" + quantity +
                ", categoryId=" + categoryId +
                '}';
    }
}