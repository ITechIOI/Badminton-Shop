package com.example.paymentservice.modules.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentDto {
    private String transactionId;
    private String paymentMethod;
    private Long orderId;
    private String status;
    private Double amount;

    @Override
    public String toString() {
        return "CreatePaymentDto: Transaction is " + this.transactionId + "\n" + "Payment method: " + this.paymentMethod + "\n"
                + "Order: " + this.orderId + "\n"
                + "Status: " + this.status;
    }
}
