package com.example.productservice.modules.Products.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductDto {
    private String name;
    private String brand;
    private String description;
    private Integer price;
    private String imageUrl;
    private String videoUrl;
    private String available;
    private Integer quantity;
    private Long categoryId;

}
