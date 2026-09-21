package com.example.universityemergencyapp;

import java.io.Serializable;

/**
 * Simple in-memory model for a campus alert.
 * No backend — all data is created locally (dummy/sample data).
 */
public class Alert implements Serializable {

    public static final String SEVERITY_CRITICAL = "CRITICAL";
    public static final String SEVERITY_WARNING = "WARNING";
    public static final String SEVERITY_INFO = "INFO";
    public static final String SEVERITY_RESOLVED = "RESOLVED";

    private final String id;
    private final String title;
    private final String description;
    private final String fullBody;
    private final String time;
    private final String severity;
    private final String category; // Critical, Weather, Hostel, Resolved
    private final String location;
    private final String postedBy;

    public Alert(String id, String title, String description, String fullBody,
                 String time, String severity, String category,
                 String location, String postedBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fullBody = fullBody;
        this.time = time;
        this.severity = severity;
        this.category = category;
        this.location = location;
        this.postedBy = postedBy;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFullBody() { return fullBody; }
    public String getTime() { return time; }
    public String getSeverity() { return severity; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getPostedBy() { return postedBy; }
}
