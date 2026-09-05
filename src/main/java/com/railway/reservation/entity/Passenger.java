package com.railway.reservation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "passengers")
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 20)
    private String gender;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 30)
    private String berthPreference; // LOWER, UPPER, MIDDLE, SIDE LOWER, SIDE UPPER, WINDOW

    @Column(length = 50)
    private String nationality = "Indian";

    @Column(length = 50)
    private String idType; // AADHAAR, PASSPORT, VOTER_ID, DRIVING_LICENSE

    @Column(length = 50)
    private String idNumber;

    @Column(length = 30)
    private String mealPreference; // VEG, NON_VEG, NONE

    public Passenger() {
    }

    public Passenger(String name, Integer age, String gender, String phone) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
    }

    public Passenger(String name, Integer age, String gender, String phone, String berthPreference, String nationality, String idType, String idNumber, String mealPreference) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBerthPreference() {
        return berthPreference;
    }

    public void setBerthPreference(String berthPreference) {
        this.berthPreference = berthPreference;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getMealPreference() {
        return mealPreference;
    }

    public void setMealPreference(String mealPreference) {
        this.mealPreference = mealPreference;
    }
}
