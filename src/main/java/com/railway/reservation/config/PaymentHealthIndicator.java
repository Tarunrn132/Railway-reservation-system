package com.railway.reservation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PaymentHealthIndicator implements HealthIndicator {

    @Value("${payment.provider:mock}")
    private String paymentProvider;

    @Value("${payment.api.key:}")
    private String paymentApiKey;

    @Override
    public Health health() {
        boolean isLiveGateway = "razorpay".equalsIgnoreCase(paymentProvider) && paymentApiKey != null && !paymentApiKey.isEmpty();
        return Health.up()
                .withDetail("gateway", paymentProvider)
                .withDetail("liveGatewayActive", isLiveGateway)
                .withDetail("serverSideVerificationEnabled", true)
                .build();
    }
}
