package com.example.paymentservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payments extends AbstractModel {
    @Column(nullable = true)
    private String transactionId;

    @Column(nullable = true)
    @Pattern(regexp = "cod|momo|zalopay|paypal", message = "The payment method is invalid")
    private String paymentMethod;

    @Column()
    private Long orderId;

    @Column()
    private Double amount;

    @Column()
    @Pattern(regexp = "pending|completed|refunded", message = "The transaction status is invalid")
    private String status;

    @Override
    public String toString() {
        return "Payments(transactionId=" + this.getTransactionId() +
                ", paymentMethod=" + this.getPaymentMethod() + ", orderId=" + this.getOrderId() +
                ", amount=" + this.getAmount() + ", status=" + this.getStatus() + ")";
    }
}
