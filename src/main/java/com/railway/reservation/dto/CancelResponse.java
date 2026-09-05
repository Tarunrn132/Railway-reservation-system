package com.railway.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class CancelResponse {
    private String pnr;
    private Long reservationId;
    private String passengerName;
    private String trainNumber;
    private String trainName;
    private String seatNumber;
    private Double originalFare;
    private Double cancellationFee;
    private Double refundAmount;
    private String status;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancellationTime;

    public CancelResponse() {
        this.cancellationTime = LocalDateTime.now();
    }

    public CancelResponse(String pnr, Long reservationId, String passengerName,
                          String trainNumber, String trainName, String seatNumber,
                          Double originalFare, Double cancellationFee, Double refundAmount,
                          String status, String message) {
        this.pnr = pnr;
        this.reservationId = reservationId;
        this.passengerName = passengerName;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.seatNumber = seatNumber;
        this.originalFare = originalFare;
        this.cancellationFee = cancellationFee;
        this.refundAmount = refundAmount;
        this.status = status;
        this.message = message;
        this.cancellationTime = LocalDateTime.now();
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Double getOriginalFare() {
        return originalFare;
    }

    public void setOriginalFare(Double originalFare) {
        this.originalFare = originalFare;
    }

    public Double getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(Double cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCancellationTime() {
        return cancellationTime;
    }

    public void setCancellationTime(LocalDateTime cancellationTime) {
        this.cancellationTime = cancellationTime;
    }
}
