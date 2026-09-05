package com.railway.reservation.dto;

public class StationDto {

    private Long id;
    private String stationCode;
    private String stationName;
    private String city;
    private String district;
    private String state;
    private String railwayZone;
    private String railwayDivision;
    private String aliases;
    private Double latitude;
    private Double longitude;
    private Boolean active;
    private String displayName;

    public StationDto() {
    }

    public StationDto(Long id, String stationCode, String stationName, String city, String state, Double latitude, Double longitude, Boolean active) {
        this.id = id;
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.city = city;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = active;
        this.displayName = formatDisplayName(stationName, stationCode, city);
    }

    public StationDto(Long id, String stationCode, String stationName, String city, String district, String state,
                      String railwayZone, String railwayDivision, String aliases,
                      Double latitude, Double longitude, Boolean active) {
        this.id = id;
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.city = city;
        this.district = district;
        this.state = state;
        this.railwayZone = railwayZone;
        this.railwayDivision = railwayDivision;
        this.aliases = aliases;
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = active;
        this.displayName = formatDisplayName(stationName, stationCode, city);
    }

    public static String formatDisplayName(String stationName, String stationCode, String city) {
        if (stationName != null && stationCode != null) {
            return stationName + " (" + stationCode + ")";
        }
        return stationName != null ? stationName : stationCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getRailwayZone() {
        return railwayZone;
    }

    public void setRailwayZone(String railwayZone) {
        this.railwayZone = railwayZone;
    }

    public String getRailwayDivision() {
        return railwayDivision;
    }

    public void setRailwayDivision(String railwayDivision) {
        this.railwayDivision = railwayDivision;
    }

    public String getAliases() {
        return aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
