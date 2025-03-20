package com.example.productservice.modules.feign.Orders;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public record OrderResponse(
        Long id,
        Integer totalPrice,
        // status is in "pending", "shipping", "completed", "cancelled"
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
