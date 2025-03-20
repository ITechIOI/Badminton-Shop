package com.example.productservice.modules.Grn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGrnDto {
    private String receiptId;
    private String receiptDate;
    private int totalPrice;
    private String status;
    private Long userId;
    private Long supplierId;
}
