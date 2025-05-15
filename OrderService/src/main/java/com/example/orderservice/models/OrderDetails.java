package com.example.orderservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_details")
@Getter
@Setter
public class OrderDetails extends AbstractModel {
    @Column(nullable = true)
    private Integer quantity;

    @Column(nullable = true)
    private Integer price;

    @Column(nullable = true)
    private Long proudctId;

    @ManyToOne()
    @JoinColumn(name = "orderId", nullable = true)
    private Orders order;

    public String toString() {
        return "OrderDetails{" +
                "quantity=" + quantity +
                ", price=" + price +
                ", proudctId=" + proudctId +
                ", order=" + order +
                '}';
    }
}
