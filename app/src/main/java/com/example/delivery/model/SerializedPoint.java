package com.example.delivery.model;

public class SerializedPoint {
    private double latitude;
    private double longitude;

    public SerializedPoint() {
    }

    public SerializedPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public static SerializedPoint fromMapKitPoint(com.yandex.mapkit.geometry.Point point) {
        return new SerializedPoint(point.getLatitude(), point.getLongitude());
    }

    public com.yandex.mapkit.geometry.Point toMapKitPoint() {
        return new com.yandex.mapkit.geometry.Point(latitude, longitude);
    }
}
