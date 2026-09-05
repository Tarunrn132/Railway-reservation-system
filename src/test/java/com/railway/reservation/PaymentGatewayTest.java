package com.railway.reservation;

import com.railway.reservation.payment.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PaymentGatewayTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MockPaymentGateway mockPaymentGateway;

    @Autowired
    private RazorpayPaymentGateway razorpayPaymentGateway;

    @Test
    @DisplayName("Payment Order Creation: Creates order with valid ID and currency")
    void testCreatePaymentOrder() {
        PaymentOrderDto order = paymentService.createOrder(1850.0, "INR", "RCP_TEST_101");

        assertNotNull(order);
        assertNotNull(order.getOrderId());
        assertTrue(order.getOrderId().contains("order_"));
        assertEquals(1850.0, order.getAmount());
        assertEquals("INR", order.getCurrency());
        assertEquals("CREATED", order.getStatus());
    }

    @Test
    @DisplayName("Mock Payment Verification: Successfully verifies mock signature")
    void testVerifyMockPayment() {
        PaymentVerifyDto req = new PaymentVerifyDto("mock_order_123456", "pay_mock_789", "sig_verified_123", 1850.0);
        boolean verified = paymentService.verifyPayment(req);

        assertTrue(verified);
    }

    @Test
    @DisplayName("Razorpay HMAC SHA256 Signature Verification: Valid signature matches expected HMAC")
    void testRazorpayHmacVerification() throws Exception {
        RazorpayPaymentGateway razorpay = new RazorpayPaymentGateway();
        razorpay.setCredentials("rzp_test_key", "rzp_test_secret");

        String orderId = "order_EKwx92EN5a5";
        String paymentId = "pay_29QQoUBi66xm2f";
        String secret = "rzp_test_secret";

        // Generate expected HMAC-SHA256
        String payload = orderId + "|" + paymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String validSignature = hexString.toString();

        boolean validResult = razorpay.verifyPayment(orderId, paymentId, validSignature, 1850.0);
        assertTrue(validResult, "Valid HMAC SHA256 signature should pass verification");

        boolean invalidResult = razorpay.verifyPayment(orderId, paymentId, "invalid_tampered_signature", 1850.0);
        assertFalse(invalidResult, "Invalid HMAC signature should fail verification");
    }
}
