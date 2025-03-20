package com.example.orderservice.modules.Orders.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderDto {
    private Integer totalPrice;
    private String status;
    private String address;
    private String phone;
    private Long userId;
    private Long discountId;

    public String toString() {
        return "CreateOrderDto(totalPrice=" + this.getTotalPrice() +
                ", status=" + this.getStatus() + ", address=" + this.getAddress() +
                ", phone=" + this.getPhone() + ", userId=" + this.getUserId() +
                ", discountId=" + this.getDiscountId() + ")";
    }
}
