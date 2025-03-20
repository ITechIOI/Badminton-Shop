package com.example.notificationservice.modules.feign.Orders;

public record OrderResponse(
        Long id,
        Integer totalPrice,
        String status,
        String address,
        String phone,
        Long userId,
        Long discountId

) {
    public String toString() {
        return "OrderResponse{" +
                "totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", userId=" + userId +
                ", discountId=" + discountId +
                '}';
    }
}
