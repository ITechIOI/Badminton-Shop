package com.example.orderservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "carts")
@Getter
@Setter
public class Carts extends AbstractModel {
    @Column(nullable = true)
    private Integer quantity;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productId;
}
