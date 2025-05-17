package com.example.orderservice.modules.OrderDetails.dto.output;

import com.example.orderservice.modules.feign.ProductFeign.ProductResponse;

public record TopProductDto(
        ProductResponse product,
        Long totalSold
) {
    @Override
    public String toString() {
        return "TopProductDto{" +
                "product=" + product.name() +
                ", totalSold=" + totalSold +
                '}';
    }
}
