package com.railway.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingRequest {

    private Long userId;

    @NotNull(message = "Train ID is required")
    private Long trainId;

    private String passengerName;
    private Integer age;
    private String gender;
    private String phone;

    @NotNull(message = "Journey date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate journeyDate;

    @NotBlank(message = "Travel class is required")
    private String travelClass;

    private String quota = "GN";
    private String paymentId;
    private String paymentOrderId;

    private List<PassengerDto> passengers = new ArrayList<>();

    public BookingRequest() {
    }

    public BookingRequest(Long userId, Long trainId, String passengerName, Integer age,
                          String gender, String phone, LocalDate journeyDate, String travelClass) {
        this.userId = userId;
        this.trainId = trainId;
        this.passengerName = passengerName;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.journeyDate = journeyDate;
        this.travelClass = travelClass;
    }

    public List<PassengerDto> getPassengers() {
        if (passengers.isEmpty() && passengerName != null && !passengerName.trim().isEmpty()) {
            passengers.add(new PassengerDto(passengerName, age != null ? age : 30, gender != null ? gender : "Male", phone != null ? phone : "9876543210"));
        }
        return passengers;
    }

    public void setPassengers(List<PassengerDto> passengers) {
        this.passengers = passengers;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long trainId) {
        this.trainId = trainId;
    }

    public String getPassengerName() {
        if (passengerName == null && !passengers.isEmpty()) {
            return passengers.get(0).getName();
        }
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public Integer getAge() {
        if (age == null && !passengers.isEmpty()) {
            return passengers.get(0).getAge();
        }
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        if (gender == null && !passengers.isEmpty()) {
            return passengers.get(0).getGender();
        }
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        if (phone == null && !passengers.isEmpty()) {
            return passengers.get(0).getPhone();
        }
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(String travelClass) {
        this.travelClass = travelClass;
    }

    public String getQuota() {
        return quota;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }
}
