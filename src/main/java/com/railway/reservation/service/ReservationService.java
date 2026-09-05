package com.railway.reservation.service;

import com.railway.reservation.dto.*;
import com.railway.reservation.entity.*;
import com.railway.reservation.exception.BadRequestException;
import com.railway.reservation.exception.BookingConflictException;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.exception.UnauthorizedException;
import com.railway.reservation.provider.ExternalRailwayBookingProvider;
import com.railway.reservation.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final TrainRepository trainRepository;
    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;
    private final SeatAllocationService seatAllocationService;
    private final PaymentRepository paymentRepository;
    private final SmsService smsService;
    private final EmailService emailService;
    private final ExternalRailwayBookingProvider externalBookingProvider;

    public ReservationService(ReservationRepository reservationRepository,
                              TrainRepository trainRepository,
                              PassengerRepository passengerRepository,
                              UserRepository userRepository,
                              SeatAllocationService seatAllocationService,
                              PaymentRepository paymentRepository,
                              SmsService smsService,
                              EmailService emailService,
                              ExternalRailwayBookingProvider externalBookingProvider) {
        this.reservationRepository = reservationRepository;
        this.trainRepository = trainRepository;
        this.passengerRepository = passengerRepository;
        this.userRepository = userRepository;
        this.seatAllocationService = seatAllocationService;
        this.paymentRepository = paymentRepository;
        this.smsService = smsService;
        this.emailService = emailService;
        this.externalBookingProvider = externalBookingProvider;
    }

    @Transactional
    public synchronized BookingResponse bookTicket(BookingRequest request) {
        if (request.getJourneyDate() == null) {
            throw new BadRequestException("Journey date is required");
        }
        if (request.getJourneyDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot book tickets for past dates");
        }

        List<PassengerDto> passengerDtos = request.getPassengers();
        if (passengerDtos == null || passengerDtos.isEmpty()) {
            throw new BadRequestException("At least one passenger is required for booking");
        }

        Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with ID: " + request.getTrainId()));

        // Double-check availability before booking
        long confirmedBookings = reservationRepository.countConfirmedBookings(train.getId(), request.getJourneyDate());
        if (confirmedBookings + passengerDtos.size() > train.getTotalSeats()) {
            throw new BookingConflictException("No seats available on train " + train.getTrainNumber() + " for date " + request.getJourneyDate());
        }

        // Idempotency: prevent duplicate booking if payment transaction was already processed
        if (request.getPaymentId() != null && !request.getPaymentId().trim().isEmpty()) {
            Optional<Reservation> existingByPayment = reservationRepository.findByPaymentId(request.getPaymentId().trim());
            if (existingByPayment.isPresent()) {
                log.info("Idempotent booking hit for paymentId: {}. Returning existing reservation reference: {}",
                        request.getPaymentId(), existingByPayment.get().getPnr());
                return mapToBookingResponse(existingByPayment.get());
            }
        }

        // If official authorized booking provider is configured and requested
        if (externalBookingProvider.isOfficialAuthorizedBookingSupported()) {
            return externalBookingProvider.createOfficialBooking(request);
        }

        // Otherwise: Local Application Reservation Mode
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }

        // 1. Create and persist passengers
        List<Passenger> savedPassengers = new ArrayList<>();
        for (PassengerDto pDto : passengerDtos) {
            Passenger p = new Passenger(
                    pDto.getName().trim(),
                    pDto.getAge(),
                    pDto.getGender().trim(),
                    pDto.getPhone() != null ? pDto.getPhone().trim() : "9876543210",
                    pDto.getBerthPreference(),
                    pDto.getNationality(),
                    pDto.getIdType(),
                    pDto.getIdNumber(),
                    pDto.getMealPreference()
            );
            savedPassengers.add(passengerRepository.save(p));
        }

        List<String> allocatedSeats = seatAllocationService.allocateSeats(
                train.getId(),
                request.getJourneyDate(),
                request.getTravelClass(),
                train.getTotalSeats(),
                passengerDtos.size()
        );

        // 2. Calculate Class Fare per passenger
        double perPassengerFare = calculateFare(train.getFare(), request.getTravelClass());
        double totalFare = perPassengerFare * passengerDtos.size();

        // 3. Generate Unique Application Booking Reference
        String pnr = seatAllocationService.generateUniquePnr();

        // 4. Create primary reservation record
        Passenger primaryPassenger = savedPassengers.get(0);
        String combinedSeats = String.join(", ", allocatedSeats);

        Reservation reservation = new Reservation(
                pnr,
                user,
                train,
                primaryPassenger,
                request.getJourneyDate(),
                combinedSeats,
                request.getTravelClass(),
                totalFare,
                ReservationStatus.CONFIRMED
        );
        reservation.setSourceStationCode(train.getSourceCode());
        reservation.setDestinationStationCode(train.getDestinationCode());
        reservation.setQuota(request.getQuota() != null ? request.getQuota() : "GN");

        // 5. Record Payment Transaction
        String paymentId = request.getPaymentId() != null ? request.getPaymentId() : "TXN_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        reservation.setPaymentId(paymentId);
        Reservation saved = reservationRepository.save(reservation);

        paymentRepository.findByTransactionId(paymentId).ifPresentOrElse(
                existingPayment -> {
                    existingPayment.setPnr(pnr);
                    existingPayment.setAmount(totalFare);
                    existingPayment.setStatus(PaymentStatus.SUCCESS);
                    paymentRepository.save(existingPayment);
                },
                () -> {
                    Payment payment = new Payment(paymentId, pnr, totalFare, PaymentStatus.SUCCESS, "MockPaymentGateway");
                    paymentRepository.save(payment);
                }
        );

        // 6. Dispatch Transactional SMS (truthful wording)
        String smsMessage = "Application reservation confirmed. Application reference: " + pnr +
                ", Train: " + train.getTrainNumber() + " (" + train.getTrainName() + "), Date: " + saved.getJourneyDate() +
                ", Seats: " + combinedSeats + " (" + saved.getTravelClass() + "), Passengers: " + passengerDtos.size() +
                ", Total Fare: Rs." + totalFare;
        smsService.sendSms(primaryPassenger.getPhone(), smsMessage);

        // 7. Dispatch Transactional Email if available (truthful wording)
        if (user != null && user.getEmail() != null && !user.getEmail().contains("internal")) {
            String emailSubject = "RailReserve - Application Reservation Confirmed (" + pnr + ")";
            String emailBody = "Dear " + primaryPassenger.getName() + ",\n\n" +
                    "Your application reservation has been confirmed.\n\n" +
                    "Booking Details:\n" +
                    "• Booking Type: Application Reservation\n" +
                    "• Application Reference: " + pnr + "\n" +
                    "• Official Indian Railways PNR: None (Application Reservation)\n" +
                    "• Train: " + train.getTrainName() + " (#" + train.getTrainNumber() + ")\n" +
                    "• Route: " + train.getSource() + " to " + train.getDestination() + "\n" +
                    "• Journey Date: " + saved.getJourneyDate() + "\n" +
                    "• Allocated Seats: " + combinedSeats + " (" + saved.getTravelClass() + ")\n" +
                    "• Number of Passengers: " + passengerDtos.size() + "\n" +
                    "• Total Fare: Rs." + totalFare + "\n" +
                    "• Status: CONFIRMED\n\n" +
                    "Thank you for choosing RailReserve.\n\n" +
                    "Notice: This is an application-generated reservation slip and does not constitute an official Indian Railways IRCTC PRS ticket.";
            emailService.sendEmail(user.getEmail(), emailSubject, emailBody);
        }

        // Update train general available seats metric
        int currentRemaining = Math.max(0, train.getTotalSeats() - (int)(confirmedBookings + passengerDtos.size()));
        train.setAvailableSeats(currentRemaining);
        trainRepository.save(train);

        BookingResponse response = mapToBookingResponse(saved);
        response.setPassengers(passengerDtos);
        return response;
    }

    public BookingResponse getReservationByPnr(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            throw new BadRequestException("PNR cannot be empty");
        }
        Reservation reservation = reservationRepository.findByPnr(pnr.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("No booking found with PNR: " + pnr.trim().toUpperCase()));
        return mapToBookingResponse(reservation);
    }

    public List<BookingResponse> getReservationsByUserId(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User ID cannot be null");
        }
        return reservationRepository.findByUserIdOrderByBookingTimeDesc(userId).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getAllReservations() {
        return reservationRepository.findAllByOrderByBookingTimeDesc().stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CancelResponse cancelTicket(String pnr) {
        return cancelTicket(pnr, null);
    }

    @Transactional
    public synchronized CancelResponse cancelTicket(String pnr, Long requestingUserId) {
        if (pnr == null || pnr.trim().isEmpty()) {
            throw new BadRequestException("PNR cannot be empty");
        }

        Reservation reservation = reservationRepository.findByPnr(pnr.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("No reservation found for PNR: " + pnr.trim().toUpperCase()));

        // Security check: If ticket was booked by a registered user, prevent unauthorized cancellation by other users
        if (reservation.getUser() != null && requestingUserId != null) {
            if (!reservation.getUser().getId().equals(requestingUserId)) {
                throw new UnauthorizedException("You are not authorized to cancel this ticket as it belongs to another user.");
            }
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Reservation with PNR " + pnr + " is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);

        // Calculate Cancellation Charges & Refund (15% deduction or flat min ₹60)
        double originalFare = saved.getFare();
        double cancellationFee = Math.round(Math.max(60.0, originalFare * 0.15) * 100.0) / 100.0;
        if (cancellationFee > originalFare) {
            cancellationFee = originalFare;
        }
        double refundAmount = Math.round((originalFare - cancellationFee) * 100.0) / 100.0;

        // Update Payment status to REFUNDED
        paymentRepository.findByPnr(saved.getPnr()).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        });

        // Dispatch Cancellation SMS
        String cancelSms = "Application reservation " + saved.getPnr() + " cancelled. Refund amount of Rs." + refundAmount +
                " initiated (Deduction: Rs." + cancellationFee + ").";
        smsService.sendSms(saved.getPassenger().getPhone(), cancelSms);

        // Update train available seats metric
        Train train = saved.getTrain();
        long confirmed = reservationRepository.countConfirmedBookings(train.getId(), saved.getJourneyDate());
        train.setAvailableSeats(Math.max(0, train.getTotalSeats() - (int) confirmed));
        trainRepository.save(train);

        return new CancelResponse(
                saved.getPnr(),
                saved.getId(),
                saved.getPassenger().getName(),
                train.getTrainNumber(),
                train.getTrainName(),
                saved.getSeatNumber(),
                originalFare,
                cancellationFee,
                refundAmount,
                ReservationStatus.CANCELLED.name(),
                "Ticket cancelled successfully. Seat " + saved.getSeatNumber() + " has been released."
        );
    }

    public double calculateFare(double baseFare, String travelClass) {
        if (travelClass == null) return baseFare;
        String normalized = travelClass.toUpperCase();
        double multiplier = 1.0;
        if (normalized.contains("1A") || normalized.contains("FIRST") || normalized.contains("1 TIER")) {
            multiplier = 2.8;
        } else if (normalized.contains("2A") || normalized.contains("2 TIER")) {
            multiplier = 2.0;
        } else if (normalized.contains("3A") || normalized.contains("3 TIER")) {
            multiplier = 1.4;
        } else if (normalized.contains("CC") || normalized.contains("CHAIR")) {
            multiplier = 1.2;
        } else if (normalized.contains("2S") || normalized.contains("SECOND")) {
            multiplier = 0.6;
        } else {
            multiplier = 1.0; // Sleeper base
        }
        return Math.round((baseFare * multiplier) * 100.0) / 100.0;
    }

    private BookingResponse mapToBookingResponse(Reservation res) {
        BookingResponse dto = new BookingResponse();
        dto.setId(res.getId());
        dto.setPnr(res.getPnr());
        dto.setApplicationBookingReference(res.getPnr());
        dto.setOfficialPnr(null);
        dto.setBookingType("APPLICATION_RESERVATION");

        if (res.getUser() != null) {
            dto.setUserId(res.getUser().getId());
            dto.setUserName(res.getUser().getName());
        }

        if (res.getPassenger() != null) {
            dto.setPassengerId(res.getPassenger().getId());
            dto.setPassengerName(res.getPassenger().getName());
            dto.setAge(res.getPassenger().getAge());
            dto.setGender(res.getPassenger().getGender());
            dto.setPhone(res.getPassenger().getPhone());

            PassengerDto pDto = new PassengerDto(
                    res.getPassenger().getName(),
                    res.getPassenger().getAge(),
                    res.getPassenger().getGender(),
                    res.getPassenger().getPhone(),
                    res.getPassenger().getBerthPreference(),
                    res.getPassenger().getNationality(),
                    res.getPassenger().getIdType(),
                    res.getPassenger().getIdNumber(),
                    res.getPassenger().getMealPreference()
            );
            dto.getPassengers().add(pDto);
        }

        if (res.getTrain() != null) {
            dto.setTrainId(res.getTrain().getId());
            dto.setTrainNumber(res.getTrain().getTrainNumber());
            dto.setTrainName(res.getTrain().getTrainName());
            dto.setSource(res.getTrain().getSource());
            dto.setDestination(res.getTrain().getDestination());
            dto.setSourceCode(res.getSourceStationCode() != null ? res.getSourceStationCode() : res.getTrain().getSourceCode());
            dto.setDestinationCode(res.getDestinationStationCode() != null ? res.getDestinationStationCode() : res.getTrain().getDestinationCode());
            dto.setDepartureTime(res.getTrain().getDepartureTime());
            dto.setArrivalTime(res.getTrain().getArrivalTime());
        }

        dto.setJourneyDate(res.getJourneyDate());
        dto.setSeatNumber(res.getSeatNumber());
        dto.setTravelClass(res.getTravelClass());
        dto.setQuota(res.getQuota() != null ? res.getQuota() : "GN");
        dto.setPaymentId(res.getPaymentId());
        dto.setOfficialPnr(res.getRailwayPNR());
        dto.setFare(res.getFare());
        dto.setStatus(res.getStatus().name());
        dto.setBookingTime(res.getBookingTime());

        return dto;
    }
}
