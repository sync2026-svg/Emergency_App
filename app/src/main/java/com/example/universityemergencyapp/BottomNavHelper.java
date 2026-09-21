package com.example.universityemergencyapp;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

/**
 * Wires up the shared layout_bottom_nav.xml (included as @id/bottomNav) so every
 * screen navigates consistently. Pure frontend navigation — no backend calls.
 */
public class BottomNavHelper {

    public enum Tab { HOME, MAP, SOS, ALERTS, PROFILE }

    public static void setup(Activity activity, Tab activeTab) {
        View bottomNav = activity.findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        View tabHome = bottomNav.findViewById(R.id.tabHome);
        View tabMap = bottomNav.findViewById(R.id.tabMap);
        View tabSos = bottomNav.findViewById(R.id.tabSos);
        View btnSos = bottomNav.findViewById(R.id.btnSos);
        View tabAlerts = bottomNav.findViewById(R.id.tabAlerts);
        View tabProfile = bottomNav.findViewById(R.id.tabProfile);

        if (tabHome != null) {
            tabHome.setOnClickListener(v -> go(activity, Home.class, activeTab, Tab.HOME));
        }
        if (tabMap != null) {
            tabMap.setOnClickListener(v -> go(activity, CampusMapActivity.class, activeTab, Tab.MAP));
        }
        if (tabAlerts != null) {
            tabAlerts.setOnClickListener(v -> go(activity, AlertsActivity.class, activeTab, Tab.ALERTS));
        }
        if (tabProfile != null) {
            tabProfile.setOnClickListener(v -> go(activity, ProfileActivity.class, activeTab, Tab.PROFILE));
        }

        View.OnClickListener sosClick = v -> {
            activity.startActivity(new Intent(activity, SosConfirmActivity.class));
        };
        if (btnSos != null) btnSos.setOnClickListener(sosClick);
        if (tabSos != null) tabSos.setOnClickListener(sosClick);
    }

    private static void go(Activity activity, Class<?> destination, Tab activeTab, Tab tabForDestination) {
        if (activeTab == tabForDestination) return; // already on this screen
        activity.startActivity(new Intent(activity, destination));
        activity.finish();
    }
}
