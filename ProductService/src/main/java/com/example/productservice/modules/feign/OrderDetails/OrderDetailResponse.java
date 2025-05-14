package com.example.productservice.modules.feign.OrderDetails;

public record OrderDetailResponse(
        Integer quantity,
        Integer price,
        Long productId,
        Long orderId
) {
    public String toString() {
        return "OrderDetailResponse{" +
                "quantity=" + quantity +
                ", price=" + price +
                ", productId=" + productId +
                ", orderId=" + orderId +
                '}';
    }
}
