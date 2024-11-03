package com.example.delivery.model;

import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;
import java.util.List;

public class RouteOrder {
    public String orderId;
    public String userId; // ID пользователя, который сделал заказ
    public String courierId; // ID курьера, который примет заказ
    public List<Point> routePoints; // Изменяем на Point
    public boolean isAccepted; // статус принятия заказа курьером
    public boolean isCompleted;
    public double totalDistance; // Общая дистанция маршрута
    public Long travelTime;

    public RouteOrder() {
    }

    public RouteOrder(String orderId, String userId, List<Point> routePoints, double totalDistance, Long travelTime) {
        this.orderId = orderId;
        this.userId = userId;
        this.routePoints = routePoints;
        this.isAccepted = false;
        this.isCompleted = false;
        this.totalDistance = 0.0;
        this.travelTime = 0L;
    }

}
