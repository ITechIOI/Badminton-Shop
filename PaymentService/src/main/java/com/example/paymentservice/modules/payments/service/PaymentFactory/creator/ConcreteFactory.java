package com.example.paymentservice.modules.payments.service.PaymentFactory.creator;

import com.example.paymentservice.modules.payments.service.PaymentFactory.IPayment;
import com.example.paymentservice.modules.payments.service.PaymentFactory.PaymentFactory;
import com.example.paymentservice.modules.payments.service.PaymentFactory.concrete.COD;
import com.example.paymentservice.modules.payments.service.PaymentFactory.concrete.Momo;
import com.example.paymentservice.modules.payments.service.PaymentFactory.concrete.Paypal;
import com.example.paymentservice.modules.payments.service.PaymentFactory.concrete.Zalopay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConcreteFactory extends PaymentFactory {

    private final Paypal paypal;
    private final Momo momo;
    private final COD cod;
    private final Zalopay zalopay;

    @Autowired
    public ConcreteFactory(Paypal paypal, Momo momo, COD cod, Zalopay zalopay) {
        this.paypal = paypal;
        this.momo = momo;
        this.cod = cod;
        this.zalopay = zalopay;
    }

    @Override
    public IPayment getPaymentMethod(String method) {
        switch (method.toLowerCase()) {
            case "paypal":
                return paypal;
            case "momo":
                return momo;
            case "cod":
                return cod;
            case "zalopay":
                return zalopay;
            default:
                throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ: " + method);
        }
    }
}
