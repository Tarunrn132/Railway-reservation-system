package com.railway.reservation.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String pnr;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, optional = false)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @Column(name = "travel_class", nullable = false, length = 100)
    private String travelClass;

    @Column(name = "source_station_code", length = 10)
    private String sourceStationCode;

    @Column(name = "destination_station_code", length = 10)
    private String destinationStationCode;

    @Column(length = 10)
    private String quota = "GN";

    @Column(name = "railway_pnr", length = 20)
    private String railwayPNR;

    @Column(name = "provider_booking_id", length = 100)
    private String providerBookingId;

    @Column(name = "payment_id", length = 100)
    private String paymentId;

    @Column(nullable = false)
    private Double fare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(name = "booking_time", nullable = false)
    private LocalDateTime bookingTime;

    public Reservation() {
    }

    public Reservation(String pnr, User user, Train train, Passenger passenger,
                       LocalDate journeyDate, String seatNumber, String travelClass,
                       Double fare, ReservationStatus status) {
        this.pnr = pnr;
        this.user = user;
        this.train = train;
        this.passenger = passenger;
        this.journeyDate = journeyDate;
        this.seatNumber = seatNumber;
        this.travelClass = travelClass;
        this.fare = fare;
        this.status = status != null ? status : ReservationStatus.CONFIRMED;
        this.bookingTime = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.bookingTime == null) {
            this.bookingTime = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ReservationStatus.CONFIRMED;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(String travelClass) {
        this.travelClass = travelClass;
    }

    public String getSourceStationCode() {
        return sourceStationCode;
    }

    public void setSourceStationCode(String sourceStationCode) {
        this.sourceStationCode = sourceStationCode;
    }

    public String getDestinationStationCode() {
        return destinationStationCode;
    }

    public void setDestinationStationCode(String destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
    }

    public String getQuota() {
        return quota;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }

    public String getRailwayPNR() {
        return railwayPNR;
    }

    public void setRailwayPNR(String railwayPNR) {
        this.railwayPNR = railwayPNR;
    }

    public String getProviderBookingId() {
        return providerBookingId;
    }

    public void setProviderBookingId(String providerBookingId) {
        this.providerBookingId = providerBookingId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Double getFare() {
        return fare;
    }

    public void setFare(Double fare) {
        this.fare = fare;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
}
