package com.example.productservice.modules.GrnDetails.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGrnDetailDto {
    private Integer quantity;
    private Integer price;
    private Long grnId;
    private Long productId;

    public String toString() {
        return "UpdateGrnDetailDto(quantity=" + this.getQuantity() + ", price=" + this.getPrice() + ", grnId=" + this.getGrnId() + ", productId=" + this.getProductId() + ")";
    }

}
