package com.railway.reservation.payment;

public interface PaymentGateway {
    String getGatewayName();
    boolean isConfigured();
    PaymentOrderDto createOrder(double amount, String currency, String receiptId);
    boolean verifyPayment(String orderId, String paymentId, String signature, double amount);
}
