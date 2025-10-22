package com.example.productservice.modules.Products.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProductDto {
    private String name;
    private String brand;
    private String description;
    private Integer price;
    private String imageUrl;
    private String videoUrl;
    private String available;
    private Integer quantity;
    private Long categoryId;

    public String toString() {
        return "UpdateProductDto(name=" + this.getName() + ", description=" + this.getDescription() +
                ", price=" + this.getPrice() + ", imageUrl=" + this.getImageUrl() +
                ", available=" + this.getAvailable() + ", quantity=" + this.getQuantity() +
                ", categoryId=" + this.getCategoryId() + ")";
    }
}
