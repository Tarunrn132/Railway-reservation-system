package com.railway.reservation.controller;

import com.railway.reservation.dto.ApiResponse;
import com.railway.reservation.dto.ProviderStatusResponse;
import com.railway.reservation.payment.PaymentService;
import com.railway.reservation.provider.ExternalRailwayBookingProvider;
import com.railway.reservation.provider.ExternalRailwayDataProvider;
import com.railway.reservation.service.AuthService;
import com.railway.reservation.service.EmailService;
import com.railway.reservation.service.SmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemHealthController {

    private final ExternalRailwayDataProvider externalRailwayDataProvider;
    private final ExternalRailwayBookingProvider externalBookingProvider;
    private final PaymentService paymentService;
    private final AuthService authService;
    private final SmsService smsService;
    private final EmailService emailService;

    public SystemHealthController(ExternalRailwayDataProvider externalRailwayDataProvider,
                                  ExternalRailwayBookingProvider externalBookingProvider,
                                  PaymentService paymentService,
                                  AuthService authService,
                                  SmsService smsService,
                                  EmailService emailService) {
        this.externalRailwayDataProvider = externalRailwayDataProvider;
        this.externalBookingProvider = externalBookingProvider;
        this.paymentService = paymentService;
        this.authService = authService;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    @GetMapping("/provider-status")
    public ResponseEntity<ApiResponse<ProviderStatusResponse>> getProviderStatus() {
        ProviderStatusResponse response = new ProviderStatusResponse();

        // 1. Train Search
        boolean searchConfigured = externalRailwayDataProvider.isSearchConfigured();
        response.setTrainSearch(new ProviderStatusResponse.ComponentStatus(
                externalRailwayDataProvider.getProviderName(),
                searchConfigured ? "CONNECTED" : "NOT_CONFIGURED",
                searchConfigured ? "External train search provider configured" : "Using authentic local master train database"
        ));

        // 2. Seat Availability
        boolean availConfigured = externalRailwayDataProvider.isAvailabilityConfigured();
        response.setAvailability(new ProviderStatusResponse.ComponentStatus(
                externalRailwayDataProvider.getProviderName(),
                availConfigured ? "CONNECTED" : "NOT_CONFIGURED",
                availConfigured ? "Live availability provider configured" : "External PRS availability API not configured (using internal database capacity)"
        ));

        // 3. Train Fare
        boolean fareConfigured = externalRailwayDataProvider.isFareConfigured();
        response.setFare(new ProviderStatusResponse.ComponentStatus(
                externalRailwayDataProvider.getProviderName(),
                fareConfigured ? "CONNECTED" : "NOT_CONFIGURED",
                fareConfigured ? "Fare provider configured" : "External fare API not configured (using telescopic calculated estimate)"
        ));

        // 4. Official PNR Status
        boolean pnrConfigured = externalRailwayDataProvider.isPnrConfigured();
        response.setPnr(new ProviderStatusResponse.ComponentStatus(
                externalRailwayDataProvider.getProviderName(),
                pnrConfigured ? "CONNECTED" : "NOT_CONFIGURED",
                pnrConfigured ? "Official PNR provider configured" : "Official Indian Railways PNR API not configured"
        ));

        // 5. Railway Booking
        boolean bookingSupported = externalBookingProvider.isOfficialAuthorizedBookingSupported();
        response.setRailwayBooking(new ProviderStatusResponse.ComponentStatus(
                externalBookingProvider.getProviderName(),
                bookingSupported ? "CONNECTED" : "NOT_CONFIGURED",
                bookingSupported ? "Official railway booking gateway configured" : "Official IRCTC PRS booking not configured (running in local application reservation mode)"
        ));

        // 6. Payment Gateway
        response.setPaymentGateway(paymentService.getProviderStatus());

        // 7. Google OAuth 2.0
        boolean googleConfigured = authService.isGoogleAuthConfigured();
        response.setGoogleOAuth(new ProviderStatusResponse.ComponentStatus(
                googleConfigured ? "CONNECTED" : "NOT_CONFIGURED"
        ));

        // 8. SMS Gateway
        response.setSms(smsService.getProviderStatus());

        // 9. Transactional Email
        response.setEmail(emailService.getProviderStatus());

        return ResponseEntity.ok(ApiResponse.success("System provider health status retrieved", response));
    }
}
