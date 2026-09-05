package com.railway.reservation.dto;

import java.time.LocalDate;
import java.util.List;

public class TrainDto {
    private Long id;
    private String trainNumber;
    private String trainName;
    private String source;
    private String destination;
    private String sourceCode;
    private String destinationCode;
    private String trainType;
    private String departureTime;
    private String arrivalTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private Double fare;
    private String trainClass;
    private Integer distanceKm;
    private String runningDays;
    private LocalDate searchDate;
    private String intermediateRoute;
    private List<String> availableClasses;

    public TrainDto() {
    }

    public TrainDto(Long id, String trainNumber, String trainName, String source,
                    String destination, String departureTime, String arrivalTime,
                    Integer totalSeats, Integer availableSeats, Double fare,
                    String trainClass, LocalDate searchDate, List<String> availableClasses) {
        this.id = id;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.fare = fare;
        this.trainClass = trainClass;
        this.searchDate = searchDate;
        this.availableClasses = availableClasses;
    }

    public TrainDto(Long id, String trainNumber, String trainName, String source,
                    String destination, String sourceCode, String destinationCode,
                    String trainType, String departureTime, String arrivalTime,
                    Integer totalSeats, Integer availableSeats, Double fare,
                    String trainClass, Integer distanceKm, String runningDays,
                    LocalDate searchDate, List<String> availableClasses) {
        this(id, trainNumber, trainName, source, destination, sourceCode, destinationCode,
             trainType, departureTime, arrivalTime, totalSeats, availableSeats, fare,
             trainClass, distanceKm, runningDays, null, searchDate, availableClasses);
    }

    public TrainDto(Long id, String trainNumber, String trainName, String source,
                    String destination, String sourceCode, String destinationCode,
                    String trainType, String departureTime, String arrivalTime,
                    Integer totalSeats, Integer availableSeats, Double fare,
                    String trainClass, Integer distanceKm, String runningDays,
                    String intermediateRoute, LocalDate searchDate, List<String> availableClasses) {
        this.id = id;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.source = source;
        this.destination = destination;
        this.sourceCode = sourceCode;
        this.destinationCode = destinationCode;
        this.trainType = trainType;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.fare = fare;
        this.trainClass = trainClass;
        this.distanceKm = distanceKm;
        this.runningDays = runningDays;
        this.intermediateRoute = intermediateRoute;
        this.searchDate = searchDate;
        this.availableClasses = availableClasses;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getDestinationCode() {
        return destinationCode;
    }

    public void setDestinationCode(String destinationCode) {
        this.destinationCode = destinationCode;
    }

    public String getTrainType() {
        return trainType;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Double getFare() {
        return fare;
    }

    public void setFare(Double fare) {
        this.fare = fare;
    }

    public String getTrainClass() {
        return trainClass;
    }

    public void setTrainClass(String trainClass) {
        this.trainClass = trainClass;
    }

    public Integer getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Integer distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getRunningDays() {
        return runningDays;
    }

    public void setRunningDays(String runningDays) {
        this.runningDays = runningDays;
    }

    public LocalDate getSearchDate() {
        return searchDate;
    }

    public void setSearchDate(LocalDate searchDate) {
        this.searchDate = searchDate;
    }

    public String getIntermediateRoute() {
        return intermediateRoute;
    }

    public void setIntermediateRoute(String intermediateRoute) {
        this.intermediateRoute = intermediateRoute;
    }

    public List<String> getAvailableClasses() {
        return availableClasses;
    }

    public void setAvailableClasses(List<String> availableClasses) {
        this.availableClasses = availableClasses;
    }
}
