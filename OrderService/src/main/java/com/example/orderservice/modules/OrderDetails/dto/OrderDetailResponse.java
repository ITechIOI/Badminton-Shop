package com.example.orderservice.modules.OrderDetails.dto;


public record OrderDetailResponse(
        Integer quantity,
        Long productId,
        Long orderId
) {
    public String toString() {
        return "OrderDetailResponse{" +
                "quantity=" + quantity +
                ", productId=" + productId +
                ", orderId=" + orderId +
                '}';
    }
}
