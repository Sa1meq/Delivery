package com.example.delivery.model;

public class Courier {
    public String id;
    public String firstName;
    public String surName;
    public String phone;
    public String typeOfCourier;
    public String rating;
    public String countOfDayDeliveres;

    public Courier(String firstName, String surName, String phone, String typeOfCourier, String rating, String countOfDayDeliveres) {
    }


    public Courier(String id, String firstName, String surName, String phone, String typeOfCourier, String rating, String countOfDayDeliveres) {
        this.id = id;
        this.firstName = firstName;
        this.surName = surName;
        this.phone = phone;
        this.typeOfCourier = typeOfCourier;
        this.rating = rating;
        this.countOfDayDeliveres = countOfDayDeliveres;
    }
}

