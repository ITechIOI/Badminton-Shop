package com.example.paymentservice.modules.payments.service.PaymentFactory;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

public interface IPayment {
    public Payment createPayments(
            Double total,
            String currency,
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException;

    public Payment executePayment(
            String paymentId,
            String payerId
    ) throws PayPalRESTException;
}
