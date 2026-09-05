package com.railway.reservation.controller;

import com.railway.reservation.dto.ApiResponse;
import com.railway.reservation.dto.BookingRequest;
import com.railway.reservation.dto.BookingResponse;
import com.railway.reservation.dto.CancelResponse;
import com.railway.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> bookTicket(@Valid @RequestBody BookingRequest request) {
        BookingResponse booking = reservationService.bookTicket(request);
        return new ResponseEntity<>(ApiResponse.success("Ticket booked successfully", booking), HttpStatus.CREATED);
    }

    @GetMapping("/{pnr}")
    public ResponseEntity<ApiResponse<BookingResponse>> getReservationByPnr(@PathVariable String pnr) {
        BookingResponse booking = reservationService.getReservationByPnr(pnr);
        return ResponseEntity.ok(ApiResponse.success("Reservation details retrieved", booking));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getReservationsByUser(@PathVariable Long userId) {
        List<BookingResponse> bookings = reservationService.getReservationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("User bookings retrieved", bookings));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllReservations() {
        List<BookingResponse> bookings = reservationService.getAllReservations();
        return ResponseEntity.ok(ApiResponse.success("All reservations retrieved", bookings));
    }

    @DeleteMapping("/{pnr}")
    public ResponseEntity<ApiResponse<CancelResponse>> cancelReservation(
            @PathVariable String pnr,
            @RequestParam(required = false) Long userId) {
        CancelResponse response = reservationService.cancelTicket(pnr, userId);
        return ResponseEntity.ok(ApiResponse.success("Ticket cancelled successfully", response));
    }

    @PostMapping("/{pnr}/cancel")
    public ResponseEntity<ApiResponse<CancelResponse>> cancelReservationPost(
            @PathVariable String pnr,
            @RequestParam(required = false) Long userId) {
        CancelResponse response = reservationService.cancelTicket(pnr, userId);
        return ResponseEntity.ok(ApiResponse.success("Ticket cancelled successfully", response));
    }
}
