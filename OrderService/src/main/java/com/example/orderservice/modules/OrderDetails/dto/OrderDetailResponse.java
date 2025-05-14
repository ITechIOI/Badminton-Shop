package com.example.orderservice.modules.OrderDetails.dto;


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
