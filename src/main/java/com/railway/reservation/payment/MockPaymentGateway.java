package com.railway.reservation.payment;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public String getGatewayName() {
        return "MockPaymentGateway";
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public PaymentOrderDto createOrder(double amount, String currency, String receiptId) {
        String orderId = "mock_order_" + UUID.randomUUID().toString().substring(0, 10);
        return new PaymentOrderDto(
                orderId,
                amount,
                currency != null ? currency : "INR",
                receiptId,
                "CREATED",
                "MockPaymentGateway",
                "mock_key_id"
        );
    }

    @Override
    public boolean verifyPayment(String orderId, String paymentId, String signature, double amount) {
        return paymentId != null && !paymentId.trim().isEmpty() && !paymentId.contains("fail");
    }
}
