package com.example.delivery.model;

import android.graphics.Point;

import java.util.List;

public class CourierLocation {
    private String id;
    private String orderId;
    private String courierId;
    private List<SerializedPoint> courierLocation;

    public CourierLocation() {
    }

    public CourierLocation(String id, String orderId, String courierId, List<SerializedPoint> courierLocation) {
        this.id = id;
        this.orderId = orderId;
        this.courierId = courierId;
        this.courierLocation = courierLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCourierId() {
        return courierId;
    }

    public void setCourierId(String courierId) {
        this.courierId = courierId;
    }

    public List<SerializedPoint> getCourierLocation() {
        return courierLocation;
    }

    public void setCourierLocation(List<SerializedPoint> courierLocation) {
        this.courierLocation = courierLocation;
    }
}
