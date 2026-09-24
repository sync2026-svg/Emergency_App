package com.example.universityemergencyapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SosRepository {

    public interface OnSosListener {
        void onSosTriggered(SosEvent event);
    }

    private static SosRepository instance;
    private final List<SosEvent> activeSosList = new ArrayList<>();
    private final List<OnSosListener> listeners = new ArrayList<>();

    private SosRepository() {
        // Sample initial SOS event for testing Authority Dashboard
        activeSosList.add(new SosEvent(
                "SOS_1001",
                "Aditi Sharma",
                "DHSGSU2026041",
                "+91 98765 43210",
                "Computer Science & Applications",
                "O+",
                "Asthma (Mild)",
                "Dr. Rajesh Sharma (Father) - +91 98765 12345",
                "Central Library Block B, DHSGSU Campus",
                23.8315,
                78.7810,
                "Just now",
                "ACTIVE"
        ));
    }

    public static synchronized SosRepository getInstance() {
        if (instance == null) {
            instance = new SosRepository();
        }
        return instance;
    }

    public void addListener(OnSosListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnSosListener listener) {
        listeners.remove(listener);
    }

    public List<SosEvent> getActiveSosList() {
        return new ArrayList<>(activeSosList);
    }

    public SosEvent triggerSosFromUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(EditProfileActivity.PREFS_NAME, Context.MODE_PRIVATE);

        String name = prefs.getString(EditProfileActivity.KEY_USER_NAME, "Aditi Sharma");
        String id = prefs.getString(EditProfileActivity.KEY_USER_ID, "DHSGSU2026041");
        String phone = prefs.getString(EditProfileActivity.KEY_USER_PHONE, "+91 98765 43210");
        String dept = prefs.getString(EditProfileActivity.KEY_USER_DEPARTMENT, "Computer Science & Applications");
        String blood = prefs.getString(MedicalInfoActivity.KEY_BLOOD_GROUP, "O+");
        String medical = prefs.getString(MedicalInfoActivity.KEY_CONDITIONS, "Asthma (Mild)");

        String pName = prefs.getString(EmergencyContactsActivity.KEY_PRIMARY_NAME, "Dr. Rajesh Sharma");
        String pPhone = prefs.getString(EmergencyContactsActivity.KEY_PRIMARY_PHONE, "+91 98765 12345");
        String pRel = prefs.getString(EmergencyContactsActivity.KEY_PRIMARY_RELATION, "Father");

        String emergencyContactStr = pName + " (" + pRel + ") - " + pPhone;

        String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        String eventId = "SOS_" + (System.currentTimeMillis() % 10000);

        // DHSGSU Campus exact coordinates simulation
        double lat = 23.8315 + (Math.random() * 0.002 - 0.001);
        double lng = 78.7810 + (Math.random() * 0.002 - 0.001);

        SosEvent newEvent = new SosEvent(
                eventId,
                name,
                id,
                phone,
                dept,
                blood,
                medical,
                emergencyContactStr,
                "DHSGSU Campus (Near Academic Block)",
                lat,
                lng,
                timeStr,
                "ACTIVE"
        );

        activeSosList.add(0, newEvent);

        for (OnSosListener listener : new ArrayList<>(listeners)) {
            listener.onSosTriggered(newEvent);
        }

        return newEvent;
    }

    @SuppressWarnings("unused")
    public void updateStatus(String sosId, String newStatus) {
        for (SosEvent event : activeSosList) {
            if (event.getId().equals(sosId)) {
                event.setStatus(newStatus);
                break;
            }
        }
    }
}
