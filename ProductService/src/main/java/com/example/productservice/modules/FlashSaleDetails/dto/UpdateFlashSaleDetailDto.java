package com.example.productservice.modules.FlashSaleDetails.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFlashSaleDetailDto {
    private Integer originalPrice;
    private Integer salePrice;
    private Integer quantity;

    public  String toString() {
        return "CreateFlashSaleDetailDto{" +
                "originalPrice=" + originalPrice +
                ", salePrice=" + salePrice +
                ", quantity=" + quantity
                + '}';
    }
}