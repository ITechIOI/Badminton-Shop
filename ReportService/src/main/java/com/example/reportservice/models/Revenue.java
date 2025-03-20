package com.example.reportservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "revenue")
@Getter
@Setter
public class Revenue extends AbstractModel {
    @Column()
    private int month;

    @Column()
    private int year;

    @Column()
    private int totalOrders;

    @Column()
    private int totalRevenue;

    @Column()
    private int cancelledOrders;

    @Column()
    private Long userId;
}
