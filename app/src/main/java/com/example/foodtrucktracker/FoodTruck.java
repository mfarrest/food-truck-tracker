package com.example.foodtrucktracker;

public class FoodTruck {
    public String type;
    public double latitude;
    public double longitude;
    public String reporter;
    public String timestamp;

    // Required empty constructor for Firebase
    public FoodTruck() {}

    public FoodTruck(String type, double latitude, double longitude, String reporter, String timestamp) {
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reporter = reporter;
        this.timestamp = timestamp;
    }
}
