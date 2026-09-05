package com.railway.reservation.controller;

import com.railway.reservation.dto.ApiResponse;
import com.railway.reservation.dto.BookingResponse;
import com.railway.reservation.entity.User;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.repository.UserRepository;
import com.railway.reservation.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final ReservationService reservationService;

    public UserController(UserRepository userRepository, ReservationService reservationService) {
        this.userRepository = userRepository;
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<User>> getUserProfile(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", user));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<User>> updateUserProfile(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (request.containsKey("name") && !request.get("name").trim().isEmpty()) {
            user.setName(request.get("name").trim());
        }
        if (request.containsKey("phone") && !request.get("phone").trim().isEmpty()) {
            user.setPhone(request.get("phone").trim());
        }

        User updated = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUserBookings(@PathVariable Long id) {
        List<BookingResponse> bookings = reservationService.getReservationsByUserId(id);
        return ResponseEntity.ok(ApiResponse.success("User bookings retrieved successfully", bookings));
    }
}
