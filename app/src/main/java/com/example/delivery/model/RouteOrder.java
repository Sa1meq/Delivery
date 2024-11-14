package com.example.delivery.model;

import com.yandex.mapkit.geometry.Point;

import java.io.Serializable;
import java.util.List;

public class RouteOrder implements Serializable {
    public String orderId;
    public String userId;
    public String courierId;
    public List<Point> routePoints;
    public boolean isAccepted;
    public boolean isCompleted;
    public double totalDistance;
    public Long travelTime;
    public String startAddress;
    public String endAddress;
    public String courierType;
    public double estimatedCost;
    public Long estimatedDeliveryTime;
    public String orderDescription;

    public RouteOrder() {}

    public RouteOrder(String orderId, String userId, String courierId, List<Point> routePoints,
                      double totalDistance, Long travelTime, String startAddress, String endAddress,
                      String courierType, double estimatedCost, Long estimatedDeliveryTime, String orderDescription) {
        this.orderId = orderId;
        this.userId = userId;
        this.courierId = courierId != null ? courierId : "unassigned";
        this.routePoints = routePoints;
        this.isAccepted = false;
        this.isCompleted = false;
        this.totalDistance = totalDistance;
        this.travelTime = travelTime;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.courierType = courierType;
        this.estimatedCost = estimatedCost;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.orderDescription = orderDescription;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourierId() {
        return courierId;
    }

    public void setCourierId(String courierId) {
        this.courierId = courierId;
    }

    public List<Point> getRoutePoints() {
        return routePoints;
    }

    public void setRoutePoints(List<Point> routePoints) {
        this.routePoints = routePoints;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Long getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(Long travelTime) {
        this.travelTime = travelTime;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String getCourierType() {
        return courierType;
    }

    public void setCourierType(String courierType) {
        this.courierType = courierType;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public Long getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(Long estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public String getOrderDescription() {
        return orderDescription;
    }

    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
