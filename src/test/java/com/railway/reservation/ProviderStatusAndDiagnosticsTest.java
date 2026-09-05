package com.railway.reservation;

import com.railway.reservation.controller.SystemHealthController;
import com.railway.reservation.dto.*;
import com.railway.reservation.provider.ExternalRailwayDataProvider;
import com.railway.reservation.service.EmailService;
import com.railway.reservation.service.ReservationService;
import com.railway.reservation.service.SmsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ProviderStatusAndDiagnosticsTest {

    @Autowired
    private SystemHealthController systemHealthController;

    @Autowired
    private ExternalRailwayDataProvider externalProvider;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private EmailService emailService;

    @Test
    @DisplayName("System Health: Provider status diagnostic returns safe unmasked status")
    void testProviderStatusEndpoint() {
        ResponseEntity<ApiResponse<ProviderStatusResponse>> entity = systemHealthController.getProviderStatus();

        assertNotNull(entity);
        assertEquals(200, entity.getStatusCode().value());
        assertNotNull(entity.getBody());
        assertTrue(entity.getBody().isSuccess());

        ProviderStatusResponse status = entity.getBody().getData();
        assertNotNull(status);
        assertNotNull(status.getTrainSearch());
        assertNotNull(status.getAvailability());
        assertNotNull(status.getFare());
        assertNotNull(status.getPnr());
        assertNotNull(status.getPaymentGateway());
        assertNotNull(status.getRailwayBooking());
        assertNotNull(status.getGoogleOAuth());
        assertNotNull(status.getSms());
        assertNotNull(status.getEmail());

        // Status values must be either CONNECTED, NOT_CONFIGURED, or ERROR
        assertTrue(status.getTrainSearch().getStatus().equals("CONNECTED") || status.getTrainSearch().getStatus().equals("NOT_CONFIGURED"));
        assertTrue(status.getGoogleOAuth().getStatus().equals("CONNECTED") || status.getGoogleOAuth().getStatus().equals("NOT_CONFIGURED"));
    }

    @Test
    @DisplayName("Booking Distinction: Local reservation produces APPLICATION_RESERVATION with null officialPnr")
    void testLocalBookingMetadataDistinction() {
        BookingRequest req = new BookingRequest();
        req.setTrainId(1L);
        req.setPassengerName("Rohan Verma");
        req.setAge(28);
        req.setGender("Male");
        req.setPhone("9876543210");
        req.setTravelClass("Sleeper (SL)");
        req.setJourneyDate(LocalDate.now().plusDays(2));

        BookingResponse response = reservationService.bookTicket(req);

        assertNotNull(response);
        assertEquals("APPLICATION_RESERVATION", response.getBookingType());
        assertNull(response.getOfficialPnr(), "Local reservation must never claim an official Indian Railways PNR");
        assertNotNull(response.getApplicationBookingReference());
        assertEquals(response.getPnr(), response.getApplicationBookingReference());
    }

    @Test
    @DisplayName("SMS and Email Service: Providers report safe status when unconfigured")
    void testNotificationServiceStatus() {
        ProviderStatusResponse.ComponentStatus smsStatus = smsService.getProviderStatus();
        assertNotNull(smsStatus);
        assertEquals("NOT_CONFIGURED", smsStatus.getStatus());

        ProviderStatusResponse.ComponentStatus emailStatus = emailService.getProviderStatus();
        assertNotNull(emailStatus);
        assertEquals("NOT_CONFIGURED", emailStatus.getStatus());
    }
}
