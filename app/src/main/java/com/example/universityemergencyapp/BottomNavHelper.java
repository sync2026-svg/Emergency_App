package com.example.universityemergencyapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

/**
 * Wires up the shared layout_bottom_nav.xml (included as @id/bottomNav) so every
 * screen navigates consistently. Ensures only the active screen's icon and label
 * are highlighted in red, while all other tabs remain inactive gray.
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

        // Highlight only the current active tab in red, set all others to gray
        updateTabStyle(activity, tabHome, R.id.labelHome, activeTab == Tab.HOME);
        updateTabStyle(activity, tabMap, R.id.labelMap, activeTab == Tab.MAP);
        updateTabStyle(activity, tabAlerts, R.id.labelAlerts, activeTab == Tab.ALERTS);
        updateTabStyle(activity, tabProfile, R.id.labelProfile, activeTab == Tab.PROFILE);

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
            SosRepository.getInstance().triggerSosFromUser(activity);
            activity.startActivity(new Intent(activity, SosActiveActivity.class));
        };
        if (btnSos != null) btnSos.setOnClickListener(sosClick);
        if (tabSos != null) tabSos.setOnClickListener(sosClick);
    }

    private static void updateTabStyle(Activity activity, View tabView, int labelId, boolean isActive) {
        if (tabView == null) return;
        ImageView icon = null;
        if (tabView instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) tabView;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof ImageView) {
                    icon = (ImageView) child;
                    break;
                }
            }
        }
        TextView label = tabView.findViewById(labelId);
        int activeColor = ContextCompat.getColor(activity, R.color.nav_active_red);
        int inactiveColor = ContextCompat.getColor(activity, R.color.nav_icon_inactive);
        int color = isActive ? activeColor : inactiveColor;

        if (icon != null) {
            icon.setImageTintList(ColorStateList.valueOf(color));
            icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        if (label != null) {
            label.setTextColor(color);
        }
    }

    private static void go(Activity activity, Class<?> destination, Tab activeTab, Tab tabForDestination) {
        if (activeTab == tabForDestination) return; // already on this screen
        Intent intent = new Intent(activity, destination);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        activity.finish();
    }
}
