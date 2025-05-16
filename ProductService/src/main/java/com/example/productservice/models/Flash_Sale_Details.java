package com.example.productservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "flash_sale_details")
@Getter
@Setter
public class Flash_Sale_Details extends AbstractModel {
    @Column(nullable = true)
    private Integer originalPrice;

    @Column(nullable = true)
    private Integer salePrice;

    @Column(nullable = true)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "flash_sale_id", nullable = false)
    private Flash_Sale flashSale;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;
}
