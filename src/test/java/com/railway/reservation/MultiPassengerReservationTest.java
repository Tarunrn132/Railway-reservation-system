package com.railway.reservation;

import com.railway.reservation.dto.BookingRequest;
import com.railway.reservation.dto.BookingResponse;
import com.railway.reservation.dto.PassengerDto;
import com.railway.reservation.service.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MultiPassengerReservationTest {

    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("Multi-Passenger Booking: Successfully allocates sequential seats for multiple passengers")
    void testMultiPassengerBooking() {
        BookingRequest req = new BookingRequest();
        req.setTrainId(1L);
        req.setPassengerName("Amit Shah");
        req.setAge(35);
        req.setGender("Male");
        req.setPhone("9876543210");
        req.setTravelClass("AC 3 Tier (3A)");
        req.setQuota("GN");
        req.setJourneyDate(LocalDate.now().plusDays(3));
        req.setPaymentId("pay_test_batch_123");

        List<PassengerDto> passengers = new ArrayList<>();
        passengers.add(new PassengerDto("Amit Shah", 35, "Male", "LOWER"));
        passengers.add(new PassengerDto("Sunita Shah", 32, "Female", "LOWER"));
        passengers.add(new PassengerDto("Aarav Shah", 8, "Male", "UPPER"));
        req.setPassengers(passengers);

        BookingResponse response = reservationService.bookTicket(req);

        assertNotNull(response);
        assertNotNull(response.getPnr());
        assertEquals("APPLICATION_RESERVATION", response.getBookingType());
        assertNull(response.getOfficialPnr());
        assertEquals("CONFIRMED", response.getStatus());

        // Verify seats allocation for 3 passengers
        assertNotNull(response.getSeatNumber());
        String[] seats = response.getSeatNumber().split(", ");
        assertEquals(3, seats.length, "Should allocate exactly 3 seats for 3 passengers");

        // Verify passengers in response
        assertNotNull(response.getPassengers());
        assertEquals(3, response.getPassengers().size());
        assertEquals("Amit Shah", response.getPassengers().get(0).getName());
        assertEquals("Sunita Shah", response.getPassengers().get(1).getName());
        assertEquals("Aarav Shah", response.getPassengers().get(2).getName());
    }

    @Test
    @DisplayName("Single Passenger Booking: Backward compatibility allocates 1 seat when passengers list is omitted")
    void testSinglePassengerBackwardCompatibility() {
        BookingRequest req = new BookingRequest();
        req.setTrainId(1L);
        req.setPassengerName("Pooja Hegde");
        req.setAge(26);
        req.setGender("Female");
        req.setPhone("9876543210");
        req.setTravelClass("Sleeper (SL)");
        req.setJourneyDate(LocalDate.now().plusDays(5));

        BookingResponse response = reservationService.bookTicket(req);

        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());
        assertNotNull(response.getSeatNumber());
        assertFalse(response.getSeatNumber().contains(","));
        assertEquals(1, response.getPassengers().size());
    }
}
