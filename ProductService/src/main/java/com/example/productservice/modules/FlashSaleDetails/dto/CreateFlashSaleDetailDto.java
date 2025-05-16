package com.example.productservice.modules.FlashSaleDetails.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlashSaleDetailDto {
    private Integer originalPrice;
    private Integer salePrice;
    private Integer quantity;

    private Long productId;
    private Long flashSaleId;

    public  String toString() {
        return "CreateFlashSaleDetailDto{" +
                "originalPrice=" + originalPrice +
                ", salePrice=" + salePrice +
                ", quantity=" + quantity +
                ", productId=" + productId +
                ", flashSaleId=" + flashSaleId +
                '}';
    }
}
