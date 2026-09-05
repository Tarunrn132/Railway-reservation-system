package com.railway.reservation.payment;

import com.railway.reservation.dto.ProviderStatusResponse;
import com.railway.reservation.entity.Payment;
import com.railway.reservation.entity.PaymentStatus;
import com.railway.reservation.exception.BadRequestException;
import com.railway.reservation.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final RazorpayPaymentGateway razorpayGateway;
    private final MockPaymentGateway mockGateway;
    private final PaymentRepository paymentRepository;

    @Value("${payment.provider:mock}")
    private String paymentProvider;

    public PaymentService(RazorpayPaymentGateway razorpayGateway,
                          MockPaymentGateway mockGateway,
                          PaymentRepository paymentRepository) {
        this.razorpayGateway = razorpayGateway;
        this.mockGateway = mockGateway;
        this.paymentRepository = paymentRepository;
    }

    public PaymentGateway getActiveGateway() {
        if ("razorpay".equalsIgnoreCase(paymentProvider) && razorpayGateway.isConfigured()) {
            return razorpayGateway;
        }
        return mockGateway;
    }

    public ProviderStatusResponse.ComponentStatus getProviderStatus() {
        if ("razorpay".equalsIgnoreCase(paymentProvider)) {
            return new ProviderStatusResponse.ComponentStatus(
                    "Razorpay",
                    razorpayGateway.isConfigured() ? "CONNECTED" : "NOT_CONFIGURED",
                    razorpayGateway.isConfigured() ? "Razorpay gateway configured" : "Razorpay API credentials missing"
            );
        }
        return new ProviderStatusResponse.ComponentStatus(
                "MockPaymentGateway",
                "CONNECTED",
                "Running in sandbox payment mode"
        );
    }

    public PaymentOrderDto createOrder(double amount, String currency, String receiptId) {
        if (amount <= 0) {
            throw new BadRequestException("Order amount must be greater than zero");
        }
        return getActiveGateway().createOrder(amount, currency, receiptId);
    }

    public boolean verifyPayment(PaymentVerifyDto verifyDto) {
        boolean verified = getActiveGateway().verifyPayment(
                verifyDto.getOrderId(),
                verifyDto.getPaymentId(),
                verifyDto.getSignature(),
                verifyDto.getAmount()
        );

        if (verified) {
            Payment payment = new Payment(
                    verifyDto.getPaymentId(),
                    verifyDto.getOrderId(),
                    verifyDto.getAmount(),
                    PaymentStatus.SUCCESS,
                    getActiveGateway().getGatewayName()
            );
            paymentRepository.save(payment);
        }

        return verified;
    }
}
