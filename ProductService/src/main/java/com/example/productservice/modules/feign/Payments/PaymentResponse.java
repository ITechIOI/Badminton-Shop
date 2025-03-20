package com.example.productservice.modules.feign.Payments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String transactionId;
    private String paymentMethod;
    private Long orderId;
    private String status;
    private Double amount;

    public String toString() {
        return "PaymentResponse: Transaction is " + this.transactionId + "\n" + "Payment method: " + this.paymentMethod + "\n"
                + "Order: " + this.orderId + "\n"
                + "Status: " + this.status;
    }
}