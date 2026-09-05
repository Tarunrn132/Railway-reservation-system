package com.railway.reservation.repository;

import com.railway.reservation.entity.Reservation;
import com.railway.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByPnr(String pnr);

    Optional<Reservation> findByPaymentId(String paymentId);

    boolean existsByPnr(String pnr);

    List<Reservation> findByUserIdOrderByBookingTimeDesc(Long userId);

    @Query("SELECT r FROM Reservation r WHERE r.train.id = :trainId AND r.journeyDate = :journeyDate AND r.status = :status")
    List<Reservation> findByTrainIdAndJourneyDateAndStatus(
            @Param("trainId") Long trainId,
            @Param("journeyDate") LocalDate journeyDate,
            @Param("status") ReservationStatus status
    );

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.train.id = :trainId AND r.journeyDate = :journeyDate AND r.status = 'CONFIRMED'")
    long countConfirmedBookings(@Param("trainId") Long trainId, @Param("journeyDate") LocalDate journeyDate);

    @Query("SELECT r.seatNumber FROM Reservation r WHERE r.train.id = :trainId AND r.journeyDate = :journeyDate AND r.status = 'CONFIRMED'")
    List<String> findOccupiedSeats(@Param("trainId") Long trainId, @Param("journeyDate") LocalDate journeyDate);

    List<Reservation> findAllByOrderByBookingTimeDesc();
}
