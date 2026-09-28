package com.example.universityemergencyapp;

public class SecurityPerson {
    private String id;
    private String name;
    private String badgeId;
    private String phone;
    private String vehicle;
    private String postLocation;
    private String status; // "Available", "On Duty", "Dispatched"

    public SecurityPerson(String id, String name, String badgeId, String phone, String vehicle, String postLocation, String status) {
        this.id = id;
        this.name = name;
        this.badgeId = badgeId;
        this.phone = phone;
        this.vehicle = vehicle;
        this.postLocation = postLocation;
        this.status = status;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getBadgeId() { return badgeId; }
    public String getPhone() { return phone; }
    public String getVehicle() { return vehicle; }
    public String getPostLocation() { return postLocation; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return name + " (" + badgeId + " · " + vehicle + ")";
    }
}
