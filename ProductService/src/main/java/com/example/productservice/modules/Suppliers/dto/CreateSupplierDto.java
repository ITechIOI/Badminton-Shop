package com.example.productservice.modules.Suppliers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplierDto {
    private String name;
    private String phone;
    private String address;
}
