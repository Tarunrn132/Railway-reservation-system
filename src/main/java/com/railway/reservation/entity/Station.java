package com.railway.reservation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stations", indexes = {
    @Index(name = "idx_station_code", columnList = "station_code"),
    @Index(name = "idx_station_city", columnList = "city"),
    @Index(name = "idx_station_name", columnList = "station_name"),
    @Index(name = "idx_station_state", columnList = "state"),
    @Index(name = "idx_station_zone", columnList = "railway_zone")
})
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code", nullable = false, unique = true, length = 10)
    private String stationCode;

    @Column(name = "station_name", nullable = false, length = 100)
    private String stationName;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(name = "railway_zone", length = 20)
    private String railwayZone;

    @Column(name = "railway_division", length = 20)
    private String railwayDivision;

    @Column(length = 500)
    private String aliases;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(nullable = false)
    private Boolean active = true;

    public Station() {
    }

    public Station(String stationCode, String stationName, String city, String state, Double latitude, Double longitude) {
        this.stationCode = stationCode != null ? stationCode.trim().toUpperCase() : null;
        this.stationName = stationName != null ? stationName.trim() : null;
        this.city = city != null ? city.trim() : null;
        this.state = state != null ? state.trim() : null;
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = true;
    }

    public Station(String stationCode, String stationName, String city, String state, Double latitude, Double longitude, Boolean active) {
        this(stationCode, stationName, city, state, latitude, longitude);
        this.active = active != null ? active : true;
    }

    public Station(String stationCode, String stationName, String city, String district, String state,
                   String railwayZone, String railwayDivision, String aliases,
                   Double latitude, Double longitude, Boolean active) {
        this.stationCode = stationCode != null ? stationCode.trim().toUpperCase() : null;
        this.stationName = stationName != null ? stationName.trim() : null;
        this.city = city != null ? city.trim() : null;
        this.district = district != null ? district.trim() : null;
        this.state = state != null ? state.trim() : null;
        this.railwayZone = railwayZone != null ? railwayZone.trim().toUpperCase() : null;
        this.railwayDivision = railwayDivision != null ? railwayDivision.trim().toUpperCase() : null;
        this.aliases = aliases != null ? aliases.trim() : null;
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = active != null ? active : true;
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
        this.stationCode = stationCode != null ? stationCode.trim().toUpperCase() : null;
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
        this.railwayZone = railwayZone != null ? railwayZone.trim().toUpperCase() : null;
    }

    public String getRailwayDivision() {
        return railwayDivision;
    }

    public void setRailwayDivision(String railwayDivision) {
        this.railwayDivision = railwayDivision != null ? railwayDivision.trim().toUpperCase() : null;
    }

    public String getAliases() {
        return aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
