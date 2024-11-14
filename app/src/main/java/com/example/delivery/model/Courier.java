package com.example.delivery.model;

public class Courier {
    public String id;
    public String firstName;
    public String surName;
    public String phone;
    public String balance;
    public String typeOfCourier;
    public String rating;
    public String countOfDayDeliveres;

    public Courier() {
    }


    public Courier(String id, String firstName, String surName, String phone, String typeOfCourier, String rating, String countOfDayDeliveres, String balance) {
        this.id = id;
        this.firstName = firstName;
        this.surName = surName;
        this.phone = phone;
        this.typeOfCourier = typeOfCourier;
        this.rating = rating;
        this.countOfDayDeliveres = countOfDayDeliveres;
        this.balance = balance;
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

    public String getRating() {
        return rating;
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

    public void setRating(String rating) {
        this.rating = rating;
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

