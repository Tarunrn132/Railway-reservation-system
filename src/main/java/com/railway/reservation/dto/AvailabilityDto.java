package com.railway.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityDto {
    private String trainNumber;
    private String trainName;
    private LocalDate journeyDate;
    private String sourceCode;
    private String destinationCode;
    private String quota; // GN, TQ, LD, etc.
    private boolean liveDataAvailable;
    private String dataSource;
    private String providerSource; // LIVE_EXTERNAL_AVAILABILITY, APPLICATION_INTERNAL_CAPACITY, NOT_CONFIGURED
    private LocalDateTime lastUpdated;
    private List<ClassAvailability> classAvailabilities = new ArrayList<>();

    public AvailabilityDto() {
        this.lastUpdated = LocalDateTime.now();
        this.quota = "GN";
    }

    public static class ClassAvailability {
        private String travelClass; // 1A, 2A, 3A, SL, CC, 2S
        private String className;   // AC 3 Tier, Sleeper, etc.
        private String status;      // AVAILABLE, RAC, WL, NOT_AVAILABLE
        private String statusDetails; // AVAILABLE-0024, RAC 18, WL 7
        private Integer availableSeats;
        private Double fare;
        private String confirmationProbability; // HIGH, MEDIUM, LOW

        public ClassAvailability() {}

        public ClassAvailability(String travelClass, String className, String status, String statusDetails, Integer availableSeats, Double fare) {
            this.travelClass = travelClass;
            this.className = className;
            this.status = status;
            this.statusDetails = statusDetails;
            this.availableSeats = availableSeats;
            this.fare = fare;
        }

        public String getTravelClass() { return travelClass; }
        public void setTravelClass(String travelClass) { this.travelClass = travelClass; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getStatusDetails() { return statusDetails; }
        public void setStatusDetails(String statusDetails) { this.statusDetails = statusDetails; }

        public Integer getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

        public Double getFare() { return fare; }
        public void setFare(Double fare) { this.fare = fare; }

        public String getConfirmationProbability() { return confirmationProbability; }
        public void setConfirmationProbability(String confirmationProbability) { this.confirmationProbability = confirmationProbability; }
    }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public LocalDate getJourneyDate() { return journeyDate; }
    public void setJourneyDate(LocalDate journeyDate) { this.journeyDate = journeyDate; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getDestinationCode() { return destinationCode; }
    public void setDestinationCode(String destinationCode) { this.destinationCode = destinationCode; }

    public String getQuota() { return quota; }
    public void setQuota(String quota) { this.quota = quota; }

    public boolean isLiveDataAvailable() { return liveDataAvailable; }
    public void setLiveDataAvailable(boolean liveDataAvailable) { this.liveDataAvailable = liveDataAvailable; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getProviderSource() { return providerSource; }
    public void setProviderSource(String providerSource) { this.providerSource = providerSource; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public List<ClassAvailability> getClassAvailabilities() { return classAvailabilities; }
    public void setClassAvailabilities(List<ClassAvailability> classAvailabilities) { this.classAvailabilities = classAvailabilities; }
}
