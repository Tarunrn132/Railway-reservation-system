package com.railway.reservation;

import com.railway.reservation.dto.*;
import com.railway.reservation.entity.ReservationStatus;
import com.railway.reservation.entity.Train;
import com.railway.reservation.entity.User;
import com.railway.reservation.exception.BadRequestException;
import com.railway.reservation.exception.BookingConflictException;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.exception.UnauthorizedException;
import com.railway.reservation.repository.ReservationRepository;
import com.railway.reservation.repository.TrainRepository;
import com.railway.reservation.repository.UserRepository;
import com.railway.reservation.service.AuthService;
import com.railway.reservation.service.ReservationService;
import com.railway.reservation.service.TrainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RailwayReservationComprehensiveTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TrainService trainService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Train testTrain1;
    private Train testTrain2;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setup() {
        testTrain1 = trainRepository.findAll().stream().findFirst().orElseGet(() -> {
            Train t = new Train("101", "Karnataka Express", "Bangalore", "Mumbai", "06:00 AM", "10:30 PM", 120, 120, 850.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)");
            return trainRepository.save(t);
        });

        testTrain2 = trainRepository.findAll().stream().filter(t -> !t.getId().equals(testTrain1.getId())).findFirst().orElseGet(() -> {
            Train t = new Train("104", "Chennai Express", "Bangalore", "Chennai", "06:00 AM", "11:30 AM", 100, 100, 420.0, "Second Sitting (2S), AC Chair Car (CC), AC 3 Tier (3A)");
            return trainRepository.save(t);
        });

        testUser1 = userRepository.findByEmail("user1_test@example.com").orElseGet(() -> {
            RegisterRequest req = new RegisterRequest("User One", "user1_test@example.com", "9998887771", "secret123", "secret123");
            AuthResponse res = authService.register(req);
            return userRepository.findById(res.getId()).get();
        });

        testUser2 = userRepository.findByEmail("user2_test@example.com").orElseGet(() -> {
            RegisterRequest req = new RegisterRequest("User Two", "user2_test@example.com", "9998887772", "secret456", "secret456");
            AuthResponse res = authService.register(req);
            return userRepository.findById(res.getId()).get();
        });
    }

    // ===================================================================
    // 1. AUTHENTICATION & SECURITY TESTS
    // ===================================================================

    @Test
    @DisplayName("Auth: Successful registration and login with BCrypt verification")
    void testRegisterAndLoginSuccess() {
        String uniqueEmail = "audit_" + System.currentTimeMillis() + "@example.com";
        RegisterRequest regReq = new RegisterRequest("Audit User", uniqueEmail, "9123456789", "Pass@123", "Pass@123");
        AuthResponse regRes = authService.register(regReq);

        assertNotNull(regRes);
        assertNotNull(regRes.getId());
        assertEquals("Audit User", regRes.getName());
        assertEquals(uniqueEmail, regRes.getEmail());
        assertNotNull(regRes.getToken());

        // Verify password is encrypted in database
        User savedUser = userRepository.findById(regRes.getId()).orElseThrow();
        assertNotEquals("Pass@123", savedUser.getPassword());
        assertTrue(savedUser.getPassword().startsWith("$2a$") || savedUser.getPassword().startsWith("$2b$"));

        // Login with valid credentials
        LoginRequest loginReq = new LoginRequest(uniqueEmail, "Pass@123");
        AuthResponse loginRes = authService.login(loginReq);
        assertNotNull(loginRes);
        assertEquals(regRes.getId(), loginRes.getId());
    }

    @Test
    @DisplayName("Auth: Duplicate email rejection")
    void testDuplicateEmailFails() {
        RegisterRequest req = new RegisterRequest("Duplicate User", testUser1.getEmail(), "9876543210", "password", "password");
        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("Auth: Password confirmation mismatch rejection")
    void testPasswordMismatchFails() {
        RegisterRequest req = new RegisterRequest("Mismatch User", "mismatch@example.com", "9876543210", "password123", "wrongpass");
        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("Auth: Invalid login password rejection")
    void testInvalidLoginPasswordFails() {
        LoginRequest req = new LoginRequest(testUser1.getEmail(), "WrongPassword999");
        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    @Test
    @DisplayName("Auth: Non-existent user login rejection")
    void testNonExistentUserLoginFails() {
        LoginRequest req = new LoginRequest("nonexistent_email_12345@domain.com", "anyPassword");
        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    // ===================================================================
    // 2. TRAIN SEARCH & STATION TESTS
    // ===================================================================

    @Test
    @DisplayName("Train Search: Filter by valid route and future date")
    void testTrainSearchSuccess() {
        LocalDate searchDate = LocalDate.now().plusDays(1);
        while (!trainService.isTrainRunningOnDate(testTrain1.getRunningDays(), searchDate)) {
            searchDate = searchDate.plusDays(1);
        }
        List<TrainDto> results = trainService.searchTrains(testTrain1.getSource(), testTrain1.getDestination(), searchDate);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(t -> t.getTrainNumber().equals(testTrain1.getTrainNumber())));
    }

    @Test
    @DisplayName("Train Search: Same source and destination rejected")
    void testSameSourceDestinationFails() {
        assertThrows(BadRequestException.class, () -> trainService.searchTrains("Bangalore", "Bangalore", LocalDate.now().plusDays(1)));
    }

    @Test
    @DisplayName("Train Search: Past journey date rejected")
    void testPastDateSearchFails() {
        assertThrows(BadRequestException.class, () -> trainService.searchTrains("Bangalore", "Mumbai", LocalDate.now().minusDays(2)));
    }

    @Test
    @DisplayName("Train Stations: Retrieve distinct sorted station catalogue")
    void testGetAllStations() {
        List<String> stations = trainService.getAllStations();
        assertNotNull(stations);
        assertTrue(stations.size() >= 2);
        assertTrue(stations.stream().anyMatch(s -> s.contains("Bengaluru") || s.contains("SBC")));
        assertTrue(stations.stream().anyMatch(s -> s.contains("Chennai") || s.contains("MAS")));
    }

    // ===================================================================
    // 3. SEAT ALLOCATION & BOOKING STRESS TESTS
    // ===================================================================

    @Test
    @DisplayName("Seat Allocation: Sequential coach seat assignment without duplicates")
    void testSequentialSeatAllocation() {
        LocalDate date = LocalDate.now().plusDays(10);
        String travelClass = "AC 3 Tier (3A)";

        BookingRequest req1 = new BookingRequest(testUser1.getId(), testTrain1.getId(), "Passenger 1", 25, "Male", "9876543210", date, travelClass);
        BookingRequest req2 = new BookingRequest(testUser1.getId(), testTrain1.getId(), "Passenger 2", 26, "Female", "9876543211", date, travelClass);
        BookingRequest req3 = new BookingRequest(testUser1.getId(), testTrain1.getId(), "Passenger 3", 27, "Male", "9876543212", date, travelClass);

        BookingResponse res1 = reservationService.bookTicket(req1);
        BookingResponse res2 = reservationService.bookTicket(req2);
        BookingResponse res3 = reservationService.bookTicket(req3);

        assertEquals("B1-01", res1.getSeatNumber());
        assertEquals("B1-02", res2.getSeatNumber());
        assertEquals("B1-03", res3.getSeatNumber());

        // Verify PNR uniqueness
        Set<String> pnrs = new HashSet<>(List.of(res1.getPnr(), res2.getPnr(), res3.getPnr()));
        assertEquals(3, pnrs.size());
    }

    @Test
    @DisplayName("Seat Allocation: Different travel classes get distinct coach prefixes")
    void testDifferentClassesPrefixes() {
        LocalDate date = LocalDate.now().plusDays(12);

        BookingResponse res1A = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "P 1A", 40, "Male", "9876543210", date, "AC First Class (1A)"));
        BookingResponse res2A = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "P 2A", 35, "Female", "9876543211", date, "AC 2 Tier (2A)"));
        BookingResponse res3A = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "P 3A", 30, "Male", "9876543212", date, "AC 3 Tier (3A)"));
        BookingResponse resSL = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "P SL", 22, "Male", "9876543213", date, "Sleeper (SL)"));

        assertEquals("H1-01", res1A.getSeatNumber());
        assertEquals("A1-01", res2A.getSeatNumber());
        assertEquals("B1-01", res3A.getSeatNumber());
        assertEquals("S1-01", resSL.getSeatNumber());
    }

    @Test
    @DisplayName("Seat Allocation: Different journey dates have independent seat pools")
    void testDifferentDatesIndependentSeats() {
        LocalDate date1 = LocalDate.now().plusDays(15);
        LocalDate date2 = LocalDate.now().plusDays(16);

        BookingResponse resDate1 = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "Traveler 1", 30, "Male", "9876543210", date1, "AC 2 Tier (2A)"));
        BookingResponse resDate2 = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "Traveler 2", 31, "Female", "9876543211", date2, "AC 2 Tier (2A)"));

        assertEquals("A1-01", resDate1.getSeatNumber());
        assertEquals("A1-01", resDate2.getSeatNumber());
    }

    @Test
    @DisplayName("Seat Allocation: Different trains have independent seat pools")
    void testDifferentTrainsIndependentSeats() {
        LocalDate date = LocalDate.now().plusDays(20);

        BookingResponse resTrain1 = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "Passenger T1", 28, "Male", "9876543210", date, "AC 3 Tier (3A)"));
        BookingResponse resTrain2 = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain2.getId(), "Passenger T2", 29, "Female", "9876543211", date, "AC 3 Tier (3A)"));

        assertEquals("B1-01", resTrain1.getSeatNumber());
        assertEquals("B1-01", resTrain2.getSeatNumber());
        assertNotEquals(resTrain1.getPnr(), resTrain2.getPnr());
    }

    // ===================================================================
    // 4. CANCELLATION, SEAT REUSE & SECURITY AUDIT
    // ===================================================================

    @Test
    @DisplayName("Cancellation: Cancel ticket releases seat and allows next booking to claim it")
    void testCancellationAndSeatReuse() {
        LocalDate date = LocalDate.now().plusDays(25);
        String travelClass = "AC 2 Tier (2A)";

        // 1. Book first ticket -> A1-01
        BookingResponse res1 = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "Alice", 25, "Female", "9876543210", date, travelClass));
        assertEquals("A1-01", res1.getSeatNumber());

        // 2. Book second ticket -> A1-02
        BookingResponse res2 = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "Bob", 30, "Male", "9876543211", date, travelClass));
        assertEquals("A1-02", res2.getSeatNumber());

        // 3. Cancel first ticket (A1-01) by owner testUser1
        CancelResponse cancelRes = reservationService.cancelTicket(res1.getPnr(), testUser1.getId());
        assertEquals("CANCELLED", cancelRes.getStatus());
        assertTrue(cancelRes.getRefundAmount() > 0);
        assertTrue(cancelRes.getCancellationFee() >= 60.0);

        // 4. Book third ticket -> Must reclaim released seat A1-01
        BookingResponse res3 = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "Charlie", 35, "Male", "9876543212", date, travelClass));
        assertEquals("A1-01", res3.getSeatNumber());

        // 5. Book fourth ticket -> Must take next unassigned seat A1-03
        BookingResponse res4 = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "David", 40, "Male", "9876543213", date, travelClass));
        assertEquals("A1-03", res4.getSeatNumber());
    }

    @Test
    @DisplayName("Cancellation: Double cancellation is rejected with BadRequestException")
    void testDoubleCancellationFails() {
        LocalDate date = LocalDate.now().plusDays(30);
        BookingResponse res = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "Dave", 32, "Male", "9876543210", date, "Sleeper (SL)"));

        reservationService.cancelTicket(res.getPnr(), testUser1.getId());
        assertThrows(BadRequestException.class, () -> reservationService.cancelTicket(res.getPnr(), testUser1.getId()));
    }

    @Test
    @DisplayName("Security: Unauthorized user cannot cancel another user's reservation")
    void testUnauthorizedCancellationFails() {
        LocalDate date = LocalDate.now().plusDays(35);
        // Booked by User 1
        BookingResponse res = reservationService.bookTicket(new BookingRequest(testUser1.getId(), testTrain1.getId(), "Owner Passenger", 35, "Male", "9876543210", date, "AC 3 Tier (3A)"));

        // User 2 attempts to cancel User 1's ticket -> Must throw UnauthorizedException
        assertThrows(UnauthorizedException.class, () -> reservationService.cancelTicket(res.getPnr(), testUser2.getId()));

        // Verify status remains CONFIRMED
        BookingResponse fetched = reservationService.getReservationByPnr(res.getPnr());
        assertEquals("CONFIRMED", fetched.getStatus());
    }

    @Test
    @DisplayName("Capacity: Booking rejected when train capacity is exceeded")
    void testCapacityExceededFails() {
        final Train tinyTrain = trainRepository.save(
                new Train("999_TINY", "Tiny Shuttler", "Bangalore", "Mysore", "08:00 AM", "10:00 AM", 2, 2, 200.0, "Sleeper (SL)")
        );

        LocalDate date = LocalDate.now().plusDays(40);
        reservationService.bookTicket(new BookingRequest(testUser1.getId(), tinyTrain.getId(), "P1", 20, "Male", "9876543210", date, "Sleeper (SL)"));
        reservationService.bookTicket(new BookingRequest(testUser1.getId(), tinyTrain.getId(), "P2", 21, "Female", "9876543211", date, "Sleeper (SL)"));

        // Third booking on a 2-seat train must fail
        assertThrows(BookingConflictException.class, () ->
                reservationService.bookTicket(new BookingRequest(testUser1.getId(), tinyTrain.getId(), "P3", 22, "Male", "9876543212", date, "Sleeper (SL)"))
        );
    }
}
