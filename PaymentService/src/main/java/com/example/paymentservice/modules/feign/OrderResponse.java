package com.example.paymentservice.modules.feign;

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
}
