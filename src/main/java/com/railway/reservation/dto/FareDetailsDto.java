package com.railway.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FareDetailsDto {
    private String trainNumber;
    private String trainName;
    private LocalDate journeyDate;
    private String sourceCode;
    private String destinationCode;
    private Integer distanceKm;
    private boolean liveDataAvailable;
    private String dataSource;
    private String providerSource; // OFFICIAL_FARE, APPLICATION_ESTIMATE, REFERENCE_FARE
    private LocalDateTime lastUpdated;
    private List<ClassFareBreakdown> classFares = new ArrayList<>();

    public FareDetailsDto() {
        this.lastUpdated = LocalDateTime.now();
    }

    public static class ClassFareBreakdown {
        private String travelClass;
        private String className;
        private Double baseFare;
        private Double reservationCharge;
        private Double superfastCharge;
        private Double dynamicPricingCharge;
        private Double gst;
        private Double cateringCharge;
        private Double totalFare;

        public ClassFareBreakdown() {}

        public ClassFareBreakdown(String travelClass, String className, Double baseFare, Double reservationCharge,
                                  Double superfastCharge, Double dynamicPricingCharge, Double gst, Double cateringCharge, Double totalFare) {
            this.travelClass = travelClass;
            this.className = className;
            this.baseFare = baseFare;
            this.reservationCharge = reservationCharge;
            this.superfastCharge = superfastCharge;
            this.dynamicPricingCharge = dynamicPricingCharge;
            this.gst = gst;
            this.cateringCharge = cateringCharge;
            this.totalFare = totalFare;
        }

        public String getTravelClass() { return travelClass; }
        public void setTravelClass(String travelClass) { this.travelClass = travelClass; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public Double getBaseFare() { return baseFare; }
        public void setBaseFare(Double baseFare) { this.baseFare = baseFare; }

        public Double getReservationCharge() { return reservationCharge; }
        public void setReservationCharge(Double reservationCharge) { this.reservationCharge = reservationCharge; }

        public Double getSuperfastCharge() { return superfastCharge; }
        public void setSuperfastCharge(Double superfastCharge) { this.superfastCharge = superfastCharge; }

        public Double getDynamicPricingCharge() { return dynamicPricingCharge; }
        public void setDynamicPricingCharge(Double dynamicPricingCharge) { this.dynamicPricingCharge = dynamicPricingCharge; }

        public Double getGst() { return gst; }
        public void setGst(Double gst) { this.gst = gst; }

        public Double getCateringCharge() { return cateringCharge; }
        public void setCateringCharge(Double cateringCharge) { this.cateringCharge = cateringCharge; }

        public Double getTotalFare() { return totalFare; }
        public void setTotalFare(Double totalFare) { this.totalFare = totalFare; }
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

    public Integer getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Integer distanceKm) { this.distanceKm = distanceKm; }

    public boolean isLiveDataAvailable() { return liveDataAvailable; }
    public void setLiveDataAvailable(boolean liveDataAvailable) { this.liveDataAvailable = liveDataAvailable; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getProviderSource() { return providerSource; }
    public void setProviderSource(String providerSource) { this.providerSource = providerSource; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public List<ClassFareBreakdown> getClassFares() { return classFares; }
    public void setClassFares(List<ClassFareBreakdown> classFares) { this.classFares = classFares; }
}
