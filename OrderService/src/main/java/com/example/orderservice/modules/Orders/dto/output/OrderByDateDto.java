package com.example.orderservice.modules.Orders.dto.output;

import com.example.orderservice.modules.feign.ProductFeign.ProductResponse;
import com.example.orderservice.modules.feign.UserFeign.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

public record OrderByDateDto(
        Long id,
        String address,
        String phone,
        Integer totalPrice,
        String paymentMethod,
        String status,
        UserResponse userResponse,
        LocalDateTime createdAt
) {
}
