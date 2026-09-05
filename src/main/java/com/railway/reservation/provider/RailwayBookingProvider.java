package com.railway.reservation.provider;

import com.railway.reservation.dto.AvailabilityDto;
import com.railway.reservation.dto.BookingRequest;
import com.railway.reservation.dto.BookingResponse;
import com.railway.reservation.dto.FareDetailsDto;
import com.railway.reservation.dto.PnrStatusDto;

import java.time.LocalDate;

public interface RailwayBookingProvider {
    String getProviderName();
    boolean isOfficialAuthorizedBookingSupported();
    AvailabilityDto checkAvailability(String trainNumber, LocalDate date, String travelClass, String quota);
    FareDetailsDto calculateFare(String trainNumber, LocalDate date, String travelClass, String quota, int passengerCount);
    BookingResponse createOfficialBooking(BookingRequest request);
    PnrStatusDto getOfficialPnrStatus(String pnr);
    boolean cancelOfficialBooking(String pnr);
}
