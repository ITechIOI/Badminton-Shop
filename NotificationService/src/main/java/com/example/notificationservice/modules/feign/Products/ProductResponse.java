package com.example.notificationservice.modules.feign.Products;

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
    public String toString() {
        return "ProductResponse{" +
                "name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", available='" + available + '\'' +
                ", quantity=" + quantity +
                ", categoryId=" + categoryId +
                '}';
    }
}