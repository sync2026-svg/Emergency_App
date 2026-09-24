package com.example.universityemergencyapp;

public class SosEvent {
    private final String id;
    private final String userName;
    private final String userId;
    private final String userPhone;
    private final String userDept;
    private final String bloodGroup;
    private final String medicalConditions;
    private final String emergencyContact;
    private final String locationLandmark;
    private final double latitude;
    private final double longitude;
    private final String timestamp;
    private String status; // "ACTIVE", "DISPATCHED", "RESOLVED"

    public SosEvent(String id, String userName, String userId, String userPhone,
                    String userDept, String bloodGroup, String medicalConditions,
                    String emergencyContact, String locationLandmark, double latitude,
                    double longitude, String timestamp, String status) {
        this.id = id;
        this.userName = userName;
        this.userId = userId;
        this.userPhone = userPhone;
        this.userDept = userDept;
        this.bloodGroup = bloodGroup;
        this.medicalConditions = medicalConditions;
        this.emergencyContact = emergencyContact;
        this.locationLandmark = locationLandmark;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getId() { return id; }
    public String getUserName() { return userName; }
    public String getUserId() { return userId; }
    public String getUserPhone() { return userPhone; }
    public String getUserDept() { return userDept; }
    public String getBloodGroup() { return bloodGroup; }
    public String getMedicalConditions() { return medicalConditions; }
    public String getEmergencyContact() { return emergencyContact; }
    public String getLocationLandmark() { return locationLandmark; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getTimestamp() { return timestamp; }

    @SuppressWarnings("unused")
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
