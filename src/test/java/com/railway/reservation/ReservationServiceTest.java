package com.railway.reservation;

import com.railway.reservation.dto.BookingRequest;
import com.railway.reservation.dto.BookingResponse;
import com.railway.reservation.dto.CancelResponse;
import com.railway.reservation.entity.ReservationStatus;
import com.railway.reservation.entity.Train;
import com.railway.reservation.exception.BadRequestException;
import com.railway.reservation.repository.TrainRepository;
import com.railway.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private TrainRepository trainRepository;

    private Train testTrain;

    @BeforeEach
    void setUp() {
        testTrain = trainRepository.findAll().stream().findFirst().orElseGet(() -> {
            Train t = new Train("999", "Test Express", "Bangalore", "Mumbai", "06:00 AM", "10:00 PM", 50, 50, 500.0, "Sleeper (SL), AC 3 Tier (3A)");
            return trainRepository.save(t);
        });
    }

    @Test
    void testBookTicketSuccess() {
        BookingRequest req = new BookingRequest(
                null,
                testTrain.getId(),
                "John Doe",
                28,
                "Male",
                "9876543210",
                LocalDate.now().plusDays(2),
                "Sleeper (SL)"
        );

        BookingResponse response = reservationService.bookTicket(req);

        assertNotNull(response);
        assertNotNull(response.getPnr());
        assertTrue(response.getPnr().startsWith("PNR"));
        assertEquals("John Doe", response.getPassengerName());
        assertEquals("CONFIRMED", response.getStatus());
        assertNotNull(response.getSeatNumber());
    }

    @Test
    void testSeatAllocationAndCancellationFlow() {
        LocalDate date = LocalDate.now().plusDays(5);
        BookingRequest req1 = new BookingRequest(
                null,
                testTrain.getId(),
                "Alice",
                25,
                "Female",
                "9123456780",
                date,
                "AC 2 Tier (2A)"
        );

        BookingResponse res1 = reservationService.bookTicket(req1);
        assertEquals("A1-01", res1.getSeatNumber());

        // Second booking should get seat A1-02
        BookingRequest req2 = new BookingRequest(
                null,
                testTrain.getId(),
                "Bob",
                30,
                "Male",
                "9123456781",
                date,
                "AC 2 Tier (2A)"
        );

        BookingResponse res2 = reservationService.bookTicket(req2);
        assertEquals("A1-02", res2.getSeatNumber());

        // Cancel first booking
        CancelResponse cancelRes = reservationService.cancelTicket(res1.getPnr());
        assertEquals("CANCELLED", cancelRes.getStatus());
        assertEquals(res1.getPnr(), cancelRes.getPnr());

        // Verify fetching cancelled ticket
        BookingResponse fetched = reservationService.getReservationByPnr(res1.getPnr());
        assertEquals("CANCELLED", fetched.getStatus());
    }

    @Test
    void testPastDateBookingThrowsException() {
        BookingRequest req = new BookingRequest(
                null,
                testTrain.getId(),
                "Past Traveler",
                25,
                "Male",
                "9123456780",
                LocalDate.now().minusDays(1),
                "Sleeper (SL)"
        );

        assertThrows(BadRequestException.class, () -> reservationService.bookTicket(req));
    }
}
