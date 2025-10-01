package com.example.orderservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Reviews extends AbstractModel{
    @Column(nullable = true)
    private Integer rating;

    @Column(nullable = true)
    private String content;

    @Column(nullable = true)
    private Long userId;

    @ManyToOne()
    @JoinColumn(name = "orderId", nullable = true)
    private Orders orders;

    @Column(nullable = true)
    private Long productId;
}
