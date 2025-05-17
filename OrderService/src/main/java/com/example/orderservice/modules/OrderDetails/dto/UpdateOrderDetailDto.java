package com.example.orderservice.modules.OrderDetails.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderDetailDto {
    private Integer quantity;
    private Integer price;  // giá hiện hành tại thời điểm mua
    private Long productId;
    private Long orderId;

    public String toString() {
        return "CreateOrderDetailDto(quantity=" + this.getQuantity() +
                ", productId=" + this.getProductId() +
                ", orderId=" + this.getOrderId() + ")";
    }

    public static record OrderDetailResponse(
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
}
