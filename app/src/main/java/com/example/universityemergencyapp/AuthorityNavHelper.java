package com.example.universityemergencyapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class AuthorityNavHelper {

    public enum Tab { DASHBOARD, HISTORY, USERS, ALERTS, LOGOUT }

    public static void setup(Activity activity, Tab activeTab) {
        View tabDashboard = activity.findViewById(R.id.tabAuthDashboard);
        View tabHistory = activity.findViewById(R.id.tabAuthHistory);
        View tabUsers = activity.findViewById(R.id.tabAuthUsers);
        View tabAlerts = activity.findViewById(R.id.tabAuthAlerts);
        View tabLogout = activity.findViewById(R.id.tabAuthLogout);

        updateTabStyle(activity, tabDashboard, R.id.labelAuthDashboard, activeTab == Tab.DASHBOARD);
        updateTabStyle(activity, tabHistory, R.id.labelAuthHistory, activeTab == Tab.HISTORY);
        updateTabStyle(activity, tabUsers, R.id.labelAuthUsers, activeTab == Tab.USERS);
        updateTabStyle(activity, tabAlerts, R.id.labelAuthAlerts, activeTab == Tab.ALERTS);
        updateTabStyle(activity, tabLogout, R.id.labelAuthLogout, activeTab == Tab.LOGOUT);

        if (tabDashboard != null) {
            tabDashboard.setOnClickListener(v -> go(activity, AuthorityDashboardActivity.class, activeTab, Tab.DASHBOARD));
        }
        if (tabHistory != null) {
            tabHistory.setOnClickListener(v -> go(activity, AuthorityHistoryActivity.class, activeTab, Tab.HISTORY));
        }
        if (tabUsers != null) {
            tabUsers.setOnClickListener(v -> go(activity, AuthorityUsersActivity.class, activeTab, Tab.USERS));
        }
        if (tabAlerts != null) {
            tabAlerts.setOnClickListener(v -> go(activity, AuthorityAlertsActivity.class, activeTab, Tab.ALERTS));
        }
        if (tabLogout != null) {
            tabLogout.setOnClickListener(v -> {
                Toast.makeText(activity, "Authority logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
            });
        }
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

    @SuppressWarnings("deprecation")
    private static void go(Activity activity, Class<?> destination, Tab activeTab, Tab tabForDestination) {
        if (activeTab == tabForDestination) return;
        Intent intent = new Intent(activity, destination);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        activity.finish();
    }
}
