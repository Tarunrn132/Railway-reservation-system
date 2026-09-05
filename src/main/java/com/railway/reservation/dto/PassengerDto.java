package com.railway.reservation.dto;

import jakarta.validation.constraints.*;

public class PassengerDto {

    @NotBlank(message = "Passenger name is required")
    @Size(min = 2, max = 100, message = "Passenger name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Passenger age is required")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age cannot exceed 120")
    private Integer age;

    @NotBlank(message = "Gender is required")
    private String gender;

    private String phone;
    private String berthPreference;
    private String nationality = "Indian";
    private String idType;
    private String idNumber;
    private String mealPreference;

    public PassengerDto() {}

    public PassengerDto(String name, Integer age, String gender, String phone) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
    }

    public PassengerDto(String name, Integer age, String gender, String phone, String berthPreference, String nationality, String idType, String idNumber, String mealPreference) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.berthPreference = berthPreference;
        this.nationality = nationality != null ? nationality : "Indian";
        this.idType = idType;
        this.idNumber = idNumber;
        this.mealPreference = mealPreference;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBerthPreference() { return berthPreference; }
    public void setBerthPreference(String berthPreference) { this.berthPreference = berthPreference; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getMealPreference() { return mealPreference; }
    public void setMealPreference(String mealPreference) { this.mealPreference = mealPreference; }
}
