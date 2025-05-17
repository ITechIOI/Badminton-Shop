package com.example.orderservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Orders extends AbstractModel{
    @Column(nullable = true)
    private Integer totalPrice;

    @Column(nullable = true)
    private String status;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String phone;

    @Column(nullable = true)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "discountId", nullable = true)
    private Discounts discount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OrderDetails> details;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reviews> reviews;

    public Orders() {}

    public String toString() {
        return "Orders{" +
                "totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", userId=" + userId +
                ", discount=" + discount +
                ", details=" + details +
                ", reviews=" + reviews +
                '}';
    }
}
