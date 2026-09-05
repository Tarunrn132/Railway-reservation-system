package com.railway.reservation.controller;

import com.railway.reservation.dto.ApiResponse;
import com.railway.reservation.payment.PaymentOrderDto;
import com.railway.reservation.payment.PaymentService;
import com.railway.reservation.payment.PaymentVerifyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentOrderDto>> createOrder(@RequestBody Map<String, Object> request) {
        double amount = Double.parseDouble(request.getOrDefault("amount", 0.0).toString());
        String currency = (String) request.getOrDefault("currency", "INR");
        String receiptId = (String) request.getOrDefault("receiptId", "REC_" + System.currentTimeMillis());

        PaymentOrderDto order = paymentService.createOrder(amount, currency, receiptId);
        return ResponseEntity.ok(ApiResponse.success("Payment order created successfully", order));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyPayment(@Valid @RequestBody PaymentVerifyDto verifyDto) {
        boolean verified = paymentService.verifyPayment(verifyDto);
        if (verified) {
            return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", true));
        } else {
            ApiResponse<Boolean> err = ApiResponse.error("Payment signature verification failed");
            err.setData(false);
            return ResponseEntity.badRequest().body(err);
        }
    }
}
