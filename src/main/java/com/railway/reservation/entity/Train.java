package com.railway.reservation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trains")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "train_number", nullable = false, unique = true, length = 20)
    private String trainNumber;

    @Column(name = "train_name", nullable = false, length = 100)
    private String trainName;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(nullable = false, length = 50)
    private String destination;

    @Column(name = "source_code", length = 10)
    private String sourceCode;

    @Column(name = "destination_code", length = 10)
    private String destinationCode;

    @Column(name = "train_type", length = 50)
    private String trainType = "Express";

    @Column(name = "departure_time", nullable = false, length = 20)
    private String departureTime;

    @Column(name = "arrival_time", nullable = false, length = 20)
    private String arrivalTime;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(nullable = false)
    private Double fare;

    @Column(name = "train_class", nullable = false, length = 255)
    private String trainClass;

    @Column(name = "distance_km")
    private Integer distanceKm;

    @Column(name = "running_days", length = 100)
    private String runningDays = "Daily";

    @Column(name = "intermediate_route", length = 1000)
    private String intermediateRoute;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 100)
    private String provider = "Indian Railways Master Timetable";

    public Train() {
    }

    public Train(String trainNumber, String trainName, String source, String destination,
                 String departureTime, String arrivalTime, Integer totalSeats,
                 Integer availableSeats, Double fare, String trainClass) {
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
        this.trainType = determineTrainType(trainName);
        this.runningDays = "Daily";
    }

    public Train(String trainNumber, String trainName, String source, String destination,
                 String sourceCode, String destinationCode, String trainType,
                 String departureTime, String arrivalTime, Integer totalSeats,
                 Integer availableSeats, Double fare, String trainClass,
                 Integer distanceKm, String runningDays, String intermediateRoute) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.source = source;
        this.destination = destination;
        this.sourceCode = sourceCode;
        this.destinationCode = destinationCode;
        this.trainType = trainType != null ? trainType : determineTrainType(trainName);
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.fare = fare;
        this.trainClass = trainClass;
        this.distanceKm = distanceKm;
        this.runningDays = runningDays != null ? runningDays : "Daily";
        this.intermediateRoute = intermediateRoute;
    }

    private String determineTrainType(String name) {
        if (name == null) return "Express";
        String upper = name.toUpperCase();
        if (upper.contains("VANDE BHARAT")) return "Vande Bharat";
        if (upper.contains("RAJDHANI")) return "Rajdhani Express";
        if (upper.contains("SHATABDI")) return "Shatabdi Express";
        if (upper.contains("DURONTO")) return "Duronto Express";
        if (upper.contains("GARIB RATH")) return "Garib Rath";
        if (upper.contains("SUPERFAST") || upper.contains("SF")) return "Superfast Express";
        return "Express";
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

    public String getIntermediateRoute() {
        return intermediateRoute;
    }

    public void setIntermediateRoute(String intermediateRoute) {
        this.intermediateRoute = intermediateRoute;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
