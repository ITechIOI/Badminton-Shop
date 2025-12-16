package com.example.productservice.modules.Products.dto;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
