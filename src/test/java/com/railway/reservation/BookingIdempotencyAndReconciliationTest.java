package com.railway.reservation;

import com.railway.reservation.dto.BookingRequest;
import com.railway.reservation.dto.BookingResponse;
import com.railway.reservation.dto.PassengerDto;
import com.railway.reservation.entity.Train;
import com.railway.reservation.repository.TrainRepository;
import com.railway.reservation.service.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookingIdempotencyAndReconciliationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private TrainRepository trainRepository;

    @Test
    @DisplayName("Should prevent duplicate booking when identical paymentId is submitted twice")
    void testBookingIdempotency() {
        Train train = trainRepository.findAll().get(0);
        String paymentId = "TXN_IDEMP_" + UUID.randomUUID().toString().substring(0, 8);

        BookingRequest request = new BookingRequest();
        request.setTrainId(train.getId());
        request.setJourneyDate(LocalDate.now().plusDays(5));
        request.setTravelClass("AC 3 Tier (3A)");
        request.setQuota("GN");
        request.setPaymentId(paymentId);

        PassengerDto p1 = new PassengerDto("Idempotency Passenger", 29, "Male", "9876543210");
        request.setPassengers(List.of(p1));

        // First attempt: should create new reservation
        BookingResponse firstResponse = reservationService.bookTicket(request);
        assertNotNull(firstResponse);
        assertNotNull(firstResponse.getPnr());
        assertEquals(paymentId, firstResponse.getPaymentId());

        // Second attempt with exact same paymentId: should return same reservation without allocating new seat
        BookingResponse secondResponse = reservationService.bookTicket(request);
        assertNotNull(secondResponse);
        assertEquals(firstResponse.getPnr(), secondResponse.getPnr());
        assertEquals(firstResponse.getSeatNumber(), secondResponse.getSeatNumber());
        assertEquals(firstResponse.getId(), secondResponse.getId());
    }
}
