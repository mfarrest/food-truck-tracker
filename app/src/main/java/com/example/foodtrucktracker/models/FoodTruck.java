package com.example.foodtrucktracker.models;

public class FoodTruck {
    public double latitude;
    public double longitude;
    public String reporter;
    public String timestamp;
    public String type;

    public FoodTruck() {
    }

    public FoodTruck(String type, double latitude, double longitude, String reporter, String timestamp) {
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reporter = reporter;
        this.timestamp = timestamp;
    }
}
