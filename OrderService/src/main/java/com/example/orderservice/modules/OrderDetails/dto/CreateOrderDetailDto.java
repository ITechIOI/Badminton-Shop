package com.example.orderservice.modules.OrderDetails.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDetailDto {
    private Integer quantity;
    private Long productId;
    private Long orderId;

    public String toString() {
        return "CreateOrderDetailDto(quantity=" + this.getQuantity() +
                ", productId=" + this.getProductId() +
                ", orderId=" + this.getOrderId() + ")";
    }
}
