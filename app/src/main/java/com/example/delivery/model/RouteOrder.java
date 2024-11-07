package com.example.delivery.model;

import com.yandex.mapkit.geometry.Point;

import java.io.Serializable;
import java.util.ArrayList;
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

    public RouteOrder() {
    }

    public RouteOrder(String orderId, String userId, String courierId, List<Point> routePoints, double totalDistance, Long travelTime) {
        this.orderId = orderId;
        this.userId = userId;
        this.courierId = courierId != null ? courierId : "unassigned";
        this.routePoints = routePoints;
        this.isAccepted = false;
        this.isCompleted = false;
        this.totalDistance = 0.0;
        this.travelTime = 0L;
    }

}
