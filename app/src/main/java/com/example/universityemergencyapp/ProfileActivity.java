package com.example.universityemergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        LinearLayout rowEditProfile = findViewById(R.id.rowEditProfile);
        LinearLayout rowEmergencyContacts = findViewById(R.id.rowEmergencyContacts);
        LinearLayout rowMedicalInfo = findViewById(R.id.rowMedicalInfo);
        LinearLayout rowSafetyGuide = findViewById(R.id.rowSafetyGuide);
        LinearLayout rowHelpCenter = findViewById(R.id.rowHelpCenter);
        LinearLayout rowMyReports = findViewById(R.id.rowMyReports);
        Switch switchLocationSharing = findViewById(R.id.switchLocationSharing);
        Switch switchPushAlerts = findViewById(R.id.switchPushAlerts);
        TextView btnLogout = findViewById(R.id.btnLogout);

        View.OnClickListener comingSoon = v ->
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();

        rowEditProfile.setOnClickListener(comingSoon);
        rowEmergencyContacts.setOnClickListener(comingSoon);
        rowMedicalInfo.setOnClickListener(comingSoon);
        rowSafetyGuide.setOnClickListener(comingSoon);
        rowHelpCenter.setOnClickListener(comingSoon);
        rowMyReports.setOnClickListener(comingSoon);

        switchLocationSharing.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                Toast.makeText(this,
                        isChecked ? "Live location sharing during SOS enabled" : "Live location sharing during SOS disabled",
                        Toast.LENGTH_SHORT).show());

        switchPushAlerts.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                Toast.makeText(this,
                        isChecked ? "Push notifications enabled" : "Push notifications disabled",
                        Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        BottomNavHelper.setup(this, BottomNavHelper.Tab.PROFILE);
    }
}
