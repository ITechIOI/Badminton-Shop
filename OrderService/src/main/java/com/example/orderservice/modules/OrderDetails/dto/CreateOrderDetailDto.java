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
    private Integer price;  // giá hiện hành tại thời điểm mua
    private Long productId;
    private Long orderId;

    public String toString() {
        return "CreateOrderDetailDto(quantity=" + this.getQuantity() +
                ", price=" + this.getPrice() +
                ", productId=" + this.getProductId() +
                ", orderId=" + this.getOrderId() + ")";
    }
}
