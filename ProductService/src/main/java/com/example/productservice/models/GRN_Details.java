package com.example.productservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "grn_details")
@Getter
@Setter
public class GRN_Details extends AbstractModel {
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @ManyToOne
    @JoinColumn(name = "grn_id", nullable = false)
    private GRN grn;

    public GRN_Details() {}

    public GRN_Details(String name) {
    }

    public String toString() {
        return "GRN_Details{" +
                "quantity='" + quantity + '\'' +
                ", price='" + price + '\'' +
                '}';
    }

}
