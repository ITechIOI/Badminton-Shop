package com.example.productservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
public class Products extends AbstractModel {
    @NotBlank(message = "Category name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Brand is required")
    @Column(nullable = false)
    private String brand;

    @Column(nullable = true)
    private String description;

    @NotBlank(message = "Price is required")
    @Column(nullable = false)
    private Integer price;

    @Column(nullable = true)
    private String imageUrl;

    @Column(nullable = true)
    private String videoUrl;

    @NotBlank(message = "Quantity is required")
    @Column(nullable = true)
    private Integer quantity;

    @Column(nullable = true, columnDefinition = "varchar(255) default 'available'")
    private String available;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Categories category;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<GRN_Details> grnDetails;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Flash_Sale_Details> flashSaleDetails;

    public Products() {}

    public Products(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Products{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", quantity=" + quantity +
                ", available='" + available + '\'' +
                ", category=" + category +
                '}';
    }

    @Builder
    public Products(String name, String brand, String description,
                    Integer price, String imageUrl, String videoUrl,
                    Integer quantity, String available, Categories category,
                    List<GRN_Details> grnDetails, List<Flash_Sale_Details> flashSaleDetails) {
        this.name = name; this.brand = brand; this.description = description;
        this.price = price; this.imageUrl = imageUrl; this.videoUrl = videoUrl;
        this.quantity = quantity; this.available = available; this.category = category;
        this.grnDetails = grnDetails; this.flashSaleDetails = flashSaleDetails;
    }

}