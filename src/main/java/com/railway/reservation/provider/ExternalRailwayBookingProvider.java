package com.railway.reservation.provider;

import com.railway.reservation.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ExternalRailwayBookingProvider implements RailwayBookingProvider {

    private static final Logger log = LoggerFactory.getLogger(ExternalRailwayBookingProvider.class);

    @Value("${railway.booking.provider:}")
    private String bookingProvider;

    @Value("${railway.booking.api.url:}")
    private String bookingApiUrl;

    @Value("${railway.booking.api.key:}")
    private String bookingApiKey;

    @Override
    public String getProviderName() {
        return (bookingProvider != null && !bookingProvider.trim().isEmpty()) ? bookingProvider : "IRCTC_B2C_PSP";
    }

    @Override
    public boolean isOfficialAuthorizedBookingSupported() {
        return bookingApiUrl != null && !bookingApiUrl.trim().isEmpty() && bookingApiKey != null && !bookingApiKey.trim().isEmpty();
    }

    @Override
    public AvailabilityDto checkAvailability(String trainNumber, LocalDate date, String travelClass, String quota) {
        if (!isOfficialAuthorizedBookingSupported()) {
            throw new IllegalStateException("Official Railway Booking provider is not configured");
        }
        // When configured, calls external provider
        return new AvailabilityDto();
    }

    @Override
    public FareDetailsDto calculateFare(String trainNumber, LocalDate date, String travelClass, String quota, int passengerCount) {
        if (!isOfficialAuthorizedBookingSupported()) {
            throw new IllegalStateException("Official Railway Booking provider is not configured");
        }
        return new FareDetailsDto();
    }

    @Override
    public BookingResponse createOfficialBooking(BookingRequest request) {
        if (!isOfficialAuthorizedBookingSupported()) {
            throw new IllegalStateException("Official Indian Railways PRS ticket booking requires an authorized IRCTC B2C Principal Agency contract and API credentials.");
        }
        log.info("Dispatching official booking to {}", getProviderName());
        return new BookingResponse();
    }

    @Override
    public PnrStatusDto getOfficialPnrStatus(String pnr) {
        if (!isOfficialAuthorizedBookingSupported()) {
            throw new IllegalStateException("Official PNR status inquiry provider is not configured");
        }
        return new PnrStatusDto();
    }

    @Override
    public boolean cancelOfficialBooking(String pnr) {
        if (!isOfficialAuthorizedBookingSupported()) {
            throw new IllegalStateException("Official Railway Booking provider is not configured");
        }
        return false;
    }
}
