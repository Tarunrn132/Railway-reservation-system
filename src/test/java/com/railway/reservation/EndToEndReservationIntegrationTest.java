package com.railway.reservation;

import com.railway.reservation.dto.*;
import com.railway.reservation.entity.ReservationStatus;
import com.railway.reservation.payment.PaymentOrderDto;
import com.railway.reservation.payment.PaymentService;
import com.railway.reservation.payment.PaymentVerifyDto;
import com.railway.reservation.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EndToEndReservationIntegrationTest {

    @Autowired
    private StationService stationService;

    @Autowired
    private TrainService trainService;

    @Autowired
    private RailwayInfoService railwayInfoService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("Complete End-to-End User Journey: Search -> Availability -> Fare -> Auth -> Payment -> Booking -> Confirmation -> Cancel")
    void testCompleteEndToEndJourney() {
        // 1. Station Search & Autocomplete
        List<StationDto> fromSuggestions = stationService.searchStations("Bangalore");
        assertFalse(fromSuggestions.isEmpty(), "Should suggest Bangalore stations");
        String srcCode = stationService.resolveStationCode("Bengaluru City Junction");
        assertEquals("SBC", srcCode);

        List<StationDto> toSuggestions = stationService.searchStations("Chennai");
        assertFalse(toSuggestions.isEmpty(), "Should suggest Chennai stations");
        String dstCode = stationService.resolveStationCode("Chennai Central");
        assertEquals("MAS", dstCode);

        // 2. Train Search for future date
        LocalDate journeyDate = LocalDate.now().plusDays(7);
        List<TrainDto> trains = trainService.searchTrains("SBC", "MAS", journeyDate);
        assertFalse(trains.isEmpty(), "Should find trains between SBC and MAS");

        TrainDto selectedTrain = trains.get(0);
        assertNotNull(selectedTrain.getTrainNumber());

        // 3. Train Schedule & Intermediate Stops
        TrainScheduleDto schedule = trainService.getTrainSchedule(selectedTrain.getTrainNumber());
        assertNotNull(schedule);
        assertTrue(schedule.getRouteStops().size() >= 2);

        // 4. Check Seat Availability
        AvailabilityDto availability = railwayInfoService.getTrainAvailability(
                selectedTrain.getTrainNumber(), journeyDate, "AC Chair Car (CC)", "GN"
        );
        assertNotNull(availability);
        assertNotNull(availability.getProviderSource());

        // 5. Check Fare Breakdown
        FareDetailsDto fare = railwayInfoService.getTrainFare(selectedTrain.getTrainNumber(), journeyDate);
        assertNotNull(fare);
        assertFalse(fare.getClassFares().isEmpty());
        assertTrue(fare.getClassFares().get(0).getTotalFare() > 0);

        // 6. User Registration & Login
        String uniqueEmail = "testuser_" + UUID.randomUUID().toString().substring(0, 8) + "@testmail.com";
        RegisterRequest registerRequest = new RegisterRequest("Test Traveler", uniqueEmail, "9876543210", "SecretPass123", "SecretPass123");
        AuthResponse registeredUser = authService.register(registerRequest);
        assertNotNull(registeredUser.getId());

        LoginRequest loginRequest = new LoginRequest(uniqueEmail, "SecretPass123");
        AuthResponse loggedInUser = authService.login(loginRequest);
        assertEquals(registeredUser.getId(), loggedInUser.getId());

        // 7. Payment Order Creation & Server-side Verification
        double totalAmount = selectedTrain.getFare() * 2;
        String receiptId = "REC_TEST_" + System.currentTimeMillis();
        PaymentOrderDto order = paymentService.createOrder(totalAmount, "INR", receiptId);
        assertNotNull(order.getOrderId());

        PaymentVerifyDto verifyReq = new PaymentVerifyDto(
                order.getOrderId(),
                "pay_mock_" + UUID.randomUUID().toString().substring(0, 10),
                "valid_test_signature",
                totalAmount
        );
        boolean verified = paymentService.verifyPayment(verifyReq);
        assertTrue(verified);

        // 8. Create Multi-Passenger Booking
        BookingRequest bookingReq = new BookingRequest();
        bookingReq.setTrainId(selectedTrain.getId());
        bookingReq.setUserId(loggedInUser.getId());
        bookingReq.setJourneyDate(journeyDate);
        bookingReq.setTravelClass("AC Chair Car (CC)");
        bookingReq.setQuota("GN");
        bookingReq.setPaymentId(verifyReq.getPaymentId());

        List<PassengerDto> passengers = new ArrayList<>();
        passengers.add(new PassengerDto("Passenger Alpha", 30, "Male", "9876543210", "LOWER", "Indian", "AADHAAR", "123456789012", "VEG"));
        passengers.add(new PassengerDto("Passenger Beta", 28, "Female", "9876543211", "UPPER", "Indian", "AADHAAR", "123456789013", "NON_VEG"));
        bookingReq.setPassengers(passengers);

        BookingResponse booking = reservationService.bookTicket(bookingReq);
        assertNotNull(booking);
        assertNotNull(booking.getApplicationBookingReference());
        assertEquals("CONFIRMED", booking.getStatus());
        assertEquals(2, booking.getPassengers().size());

        // 9. Verify separation of Application Reference vs Official PNR
        assertNotNull(booking.getApplicationBookingReference());
        assertEquals("APPLICATION_RESERVATION", booking.getBookingType());

        // 10. View User Bookings History
        List<BookingResponse> userBookings = reservationService.getReservationsByUserId(loggedInUser.getId());
        assertFalse(userBookings.isEmpty());
        assertEquals(booking.getApplicationBookingReference(), userBookings.get(0).getApplicationBookingReference());

        // 11. Booking Cancellation
        CancelResponse cancelledBooking = reservationService.cancelTicket(booking.getApplicationBookingReference(), loggedInUser.getId());
        assertEquals(ReservationStatus.CANCELLED.name(), cancelledBooking.getStatus());
    }
}
