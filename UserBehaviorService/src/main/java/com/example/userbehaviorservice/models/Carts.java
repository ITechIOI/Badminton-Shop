package com.example.userbehaviorservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
