package com.example.orderservice.modules.OrderDetails.dto.output;

public record RawTopProductDto(
        Long productId,
        Long totalSold
) {
    public String toString() {
        return "RawTopProductDto{" +
                "productId=" + productId +
                ", totalSold=" + totalSold +
                '}';
    }
}
