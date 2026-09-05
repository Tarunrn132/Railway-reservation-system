package com.railway.reservation.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Component
public class RazorpayPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(RazorpayPaymentGateway.class);

    @Value("${payment.api.key:}")
    private String apiKey;

    @Value("${payment.api.secret:}")
    private String apiSecret;

    public void setCredentials(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public String getGatewayName() {
        return "Razorpay";
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && apiSecret != null && !apiSecret.trim().isEmpty();
    }

    @Override
    public PaymentOrderDto createOrder(double amount, String currency, String receiptId) {
        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        log.info("Creating Razorpay order {} for receipt {}", orderId, receiptId);
        return new PaymentOrderDto(
                orderId,
                amount,
                currency != null ? currency : "INR",
                receiptId,
                "CREATED",
                "Razorpay",
                apiKey
        );
    }

    @Override
    public boolean verifyPayment(String orderId, String paymentId, String signature, double amount) {
        if (!isConfigured()) {
            log.warn("Razorpay credentials not configured. Verification rejected.");
            return false;
        }

        if (signature == null || signature.trim().isEmpty()) {
            log.error("Missing payment signature for order {}", orderId);
            return false;
        }

        try {
            String payload = orderId + "|" + paymentId;
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String generatedSignature = hexString.toString();

            return MessageDigest.isEqual(generatedSignature.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error verifying Razorpay signature: {}", e.getMessage());
            return false;
        }
    }
}
