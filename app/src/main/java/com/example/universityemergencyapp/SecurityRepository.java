package com.example.universityemergencyapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class SecurityRepository {

    private static final String PREFS_NAME = "security_personnel_prefs";
    private static final String KEY_PERSONNEL_COUNT = "count";
    private static SecurityRepository instance;

    private final List<SecurityPerson> securityList = new ArrayList<>();

    private SecurityRepository(Context context) {
        loadDefaultSecurityPersonnel(context);
    }

    public static synchronized SecurityRepository getInstance(Context context) {
        if (instance == null) {
            instance = new SecurityRepository(context.getApplicationContext());
        }
        return instance;
    }

    private void loadDefaultSecurityPersonnel(Context context) {
        securityList.clear();

        // Standard Default Campus Security Officers
        securityList.add(new SecurityPerson("SEC_1", "Officer Vikram Singh", "CP-104", "+91 98765 11223", "Patrol Bike #CP-104", "Main Gate 1 Command", "Available"));
        securityList.add(new SecurityPerson("SEC_2", "Officer Ramesh Kumar", "CP-108", "+91 98765 22334", "Patrol SUV #CP-108", "Hostel Block Gate", "Available"));
        securityList.add(new SecurityPerson("SEC_3", "Officer Suresh Patel", "CP-112", "+91 98765 33445", "Foot Patrol #CP-112", "Library & Admin Block", "On Duty"));
        securityList.add(new SecurityPerson("SEC_4", "Officer Ankit Sharma", "CP-115", "+91 98765 44556", "Patrol Bike #CP-115", "Lake Block Patrol", "Available"));

        // Load any additional user added guards from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int count = prefs.getInt(KEY_PERSONNEL_COUNT, 0);
        for (int i = 0; i < count; i++) {
            String name = prefs.getString("name_" + i, "");
            String badge = prefs.getString("badge_" + i, "");
            String phone = prefs.getString("phone_" + i, "");
            String vehicle = prefs.getString("vehicle_" + i, "");
            String post = prefs.getString("post_" + i, "");

            if (!name.isEmpty()) {
                securityList.add(new SecurityPerson(
                        "SEC_CUSTOM_" + i,
                        name,
                        badge.isEmpty() ? "CP-Custom" : badge,
                        phone.isEmpty() ? "+91 98000 00000" : phone,
                        vehicle.isEmpty() ? "Patrol Bike" : vehicle,
                        post.isEmpty() ? "Campus Gate" : post,
                        "Available"
                ));
            }
        }
    }

    public List<SecurityPerson> getSecurityPersons() {
        return securityList;
    }

    public void addSecurityPerson(Context context, SecurityPerson person) {
        if (person == null) return;
        securityList.add(0, person);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int count = prefs.getInt(KEY_PERSONNEL_COUNT, 0);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name_" + count, person.getName());
        editor.putString("badge_" + count, person.getBadgeId());
        editor.putString("phone_" + count, person.getPhone());
        editor.putString("vehicle_" + count, person.getVehicle());
        editor.putString("post_" + count, person.getPostLocation());
        editor.putInt(KEY_PERSONNEL_COUNT, count + 1);
        editor.apply();
    }

    public List<String> getSecurityNamesList() {
        List<String> names = new ArrayList<>();
        for (SecurityPerson sp : securityList) {
            names.add(sp.getName() + " (" + sp.getBadgeId() + " · " + sp.getVehicle() + ")");
        }
        return names;
    }

    public void deleteSecurityPerson(Context context, SecurityPerson person) {
        if (person == null) return;
        securityList.remove(person);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        int customCount = 0;
        for (SecurityPerson sp : securityList) {
            if (sp.getId().startsWith("SEC_CUSTOM_")) {
                editor.putString("name_" + customCount, sp.getName());
                editor.putString("badge_" + customCount, sp.getBadgeId());
                editor.putString("phone_" + customCount, sp.getPhone());
                editor.putString("vehicle_" + customCount, sp.getVehicle());
                editor.putString("post_" + customCount, sp.getPostLocation());
                customCount++;
            }
        }
        editor.putInt(KEY_PERSONNEL_COUNT, customCount);
        editor.apply();
    }
}
