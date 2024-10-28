package com.example.delivery.model;

import com.yandex.mapkit.geometry.Point;

import java.util.List;

public class RouteOrder {
    public String orderId;
    public String userId; // ID пользователя, который сделал заказ
    public String courierId; // ID курьера, который примет заказ
    public List<Point> routePoints; // Изменяем на Point
    public boolean isAccepted; // статус принятия заказа курьером
    public boolean isCompleted;

    public RouteOrder() {
    }

    public RouteOrder(String orderId, String userId, List<Point> routePoints) {
        this.orderId = orderId;
        this.userId = userId;
        this.routePoints = routePoints;
        this.isAccepted = false;
        this.isCompleted = false;
    }
}