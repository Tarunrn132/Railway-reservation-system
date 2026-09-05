package com.railway.reservation.dto;

import java.util.List;

public class TrainScheduleDto {

    private String trainNumber;
    private String trainName;
    private String trainType;
    private String source;
    private String destination;
    private String sourceCode;
    private String destinationCode;
    private String departureTime;
    private String arrivalTime;
    private String runningDays;
    private Integer distanceKm;
    private List<StationStopDto> routeStops;

    public TrainScheduleDto() {
    }

    public TrainScheduleDto(String trainNumber, String trainName, String trainType,
                            String source, String destination, String sourceCode,
                            String destinationCode, String departureTime, String arrivalTime,
                            String runningDays, Integer distanceKm, List<StationStopDto> routeStops) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.trainType = trainType;
        this.source = source;
        this.destination = destination;
        this.sourceCode = sourceCode;
        this.destinationCode = destinationCode;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.runningDays = runningDays;
        this.distanceKm = distanceKm;
        this.routeStops = routeStops;
    }

    public static class StationStopDto {
        private int stopNumber;
        private String stationCode;
        private String stationName;
        private String city;
        private String state;
        private Integer distanceKm;
        private String arrivalTime;
        private String departureTime;

        public StationStopDto() {
        }

        public StationStopDto(int stopNumber, String stationCode, String stationName, String city, String state,
                              Integer distanceKm, String arrivalTime, String departureTime) {
            this.stopNumber = stopNumber;
            this.stationCode = stationCode;
            this.stationName = stationName;
            this.city = city;
            this.state = state;
            this.distanceKm = distanceKm;
            this.arrivalTime = arrivalTime;
            this.departureTime = departureTime;
        }

        public int getStopNumber() {
            return stopNumber;
        }

        public void setStopNumber(int stopNumber) {
            this.stopNumber = stopNumber;
        }

        public String getStationCode() {
            return stationCode;
        }

        public void setStationCode(String stationCode) {
            this.stationCode = stationCode;
        }

        public String getStationName() {
            return stationName;
        }

        public void setStationName(String stationName) {
            this.stationName = stationName;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Integer getDistanceKm() {
            return distanceKm;
        }

        public void setDistanceKm(Integer distanceKm) {
            this.distanceKm = distanceKm;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(String arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public void setDepartureTime(String departureTime) {
            this.departureTime = departureTime;
        }
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

    public String getTrainType() {
        return trainType;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
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

    public String getRunningDays() {
        return runningDays;
    }

    public void setRunningDays(String runningDays) {
        this.runningDays = runningDays;
    }

    public Integer getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Integer distanceKm) {
        this.distanceKm = distanceKm;
    }

    public List<StationStopDto> getRouteStops() {
        return routeStops;
    }

    public void setRouteStops(List<StationStopDto> routeStops) {
        this.routeStops = routeStops;
    }
}
