package com.railway.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PnrStatusDto {
    private String pnr;
    private String trainNumber;
    private String trainName;
    private LocalDate journeyDate;
    private String sourceStation;
    private String sourceCode;
    private String destinationStation;
    private String destinationCode;
    private String boardingStation;
    private String reservationClass;
    private String quota;
    private String chartStatus; // CHART PREPARED, CHART NOT PREPARED
    private String bookingStatus; // CONFIRMED, CANCELLED, PARTIALLY_CONFIRMED
    private boolean liveDataAvailable;
    private String dataSource;
    private String errorMessage;
    private LocalDateTime lastUpdated;
    private List<PassengerPnrStatus> passengerStatuses = new ArrayList<>();

    public PnrStatusDto() {
        this.lastUpdated = LocalDateTime.now();
    }

    public static class PassengerPnrStatus {
        private Integer passengerNumber;
        private String bookingStatus; // CNF/B1/24, RAC 12, WL 4
        private String currentStatus; // CNF/B1/24, RAC 10, WL 2
        private String coach;
        private String berth;
        private String berthType; // LOWER, MIDDLE, UPPER, SIDE LOWER, SIDE UPPER

        public PassengerPnrStatus() {}

        public PassengerPnrStatus(Integer passengerNumber, String bookingStatus, String currentStatus, String coach, String berth, String berthType) {
            this.passengerNumber = passengerNumber;
            this.bookingStatus = bookingStatus;
            this.currentStatus = currentStatus;
            this.coach = coach;
            this.berth = berth;
            this.berthType = berthType;
        }

        public Integer getPassengerNumber() { return passengerNumber; }
        public void setPassengerNumber(Integer passengerNumber) { this.passengerNumber = passengerNumber; }

        public String getBookingStatus() { return bookingStatus; }
        public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

        public String getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

        public String getCoach() { return coach; }
        public void setCoach(String coach) { this.coach = coach; }

        public String getBerth() { return berth; }
        public void setBerth(String berth) { this.berth = berth; }

        public String getBerthType() { return berthType; }
        public void setBerthType(String berthType) { this.berthType = berthType; }
    }

    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public LocalDate getJourneyDate() { return journeyDate; }
    public void setJourneyDate(LocalDate journeyDate) { this.journeyDate = journeyDate; }

    public String getSourceStation() { return sourceStation; }
    public void setSourceStation(String sourceStation) { this.sourceStation = sourceStation; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getDestinationStation() { return destinationStation; }
    public void setDestinationStation(String destinationStation) { this.destinationStation = destinationStation; }

    public String getDestinationCode() { return destinationCode; }
    public void setDestinationCode(String destinationCode) { this.destinationCode = destinationCode; }

    public String getBoardingStation() { return boardingStation; }
    public void setBoardingStation(String boardingStation) { this.boardingStation = boardingStation; }

    public String getReservationClass() { return reservationClass; }
    public void setReservationClass(String reservationClass) { this.reservationClass = reservationClass; }

    public String getQuota() { return quota; }
    public void setQuota(String quota) { this.quota = quota; }

    public String getChartStatus() { return chartStatus; }
    public void setChartStatus(String chartStatus) { this.chartStatus = chartStatus; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public boolean isLiveDataAvailable() { return liveDataAvailable; }
    public void setLiveDataAvailable(boolean liveDataAvailable) { this.liveDataAvailable = liveDataAvailable; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public List<PassengerPnrStatus> getPassengerStatuses() { return passengerStatuses; }
    public void setPassengerStatuses(List<PassengerPnrStatus> passengerStatuses) { this.passengerStatuses = passengerStatuses; }
}
