package com.example.delivery.model;

public class Courier {
    public String id;
    public String firstName;
    public String surName;
    public String phone;
    public String balance;
    public String typeOfCourier;
    private float rating;
    private int ratingCount;
    public String countOfDayDeliveres;
    public boolean isVerified;

    public Courier() {
    }


    public Courier(String id, String firstName, String surName, String phone, String typeOfCourier, float rating, String countOfDayDeliveres, String balance, int ratingCount, boolean isVerified) {
        this.id = id;
        this.firstName = firstName;
        this.surName = surName;
        this.phone = phone;
        this.typeOfCourier = typeOfCourier;
        this.rating = rating;
        this.countOfDayDeliveres = countOfDayDeliveres;
        this.balance = balance;
        this.ratingCount = ratingCount;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verifed) {
        isVerified = verifed;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurName() {
        return surName;
    }

    public String getPhone() {
        return phone;
    }

    public String getTypeOfCourier() {
        return typeOfCourier;
    }


    public String getCountOfDayDeliveres() {
        return countOfDayDeliveres;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTypeOfCourier(String typeOfCourier) {
        this.typeOfCourier = typeOfCourier;
    }

    public float getRating() {
        return rating;
    }

    public void setCountOfDayDeliveres(String countOfDayDeliveres) {
        this.countOfDayDeliveres = countOfDayDeliveres;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}

