package com.example.orderservice.modules.Carts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCartDto {
    private Integer quantity;
    private Long productId;
    private Long userId;

    @Override
    public String toString() {
        return "CreateCardDto(quantity=" + this.getQuantity() + ", productId=" + this.getProductId() + ", userId=" + this.getUserId() + ")";
    }

}
