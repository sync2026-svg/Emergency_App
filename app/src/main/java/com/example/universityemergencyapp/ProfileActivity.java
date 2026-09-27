package com.example.universityemergencyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName;
    private TextView tvUserMeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserMeta = findViewById(R.id.tvUserMeta);

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

        rowEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        rowEmergencyContacts.setOnClickListener(v ->
                startActivity(new Intent(this, EmergencyContactsActivity.class)));

        rowMedicalInfo.setOnClickListener(v ->
                startActivity(new Intent(this, MedicalInfoActivity.class)));

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

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileHeader();
    }

    private void loadProfileHeader() {
        SharedPreferences prefs = getSharedPreferences(EditProfileActivity.PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(EditProfileActivity.KEY_USER_NAME, "Aditi Sharma");
        String id = prefs.getString(EditProfileActivity.KEY_USER_ID, "DHSGSU2026041");
        if (tvUserName != null) tvUserName.setText(name);
        if (tvUserMeta != null) tvUserMeta.setText("Student · ID " + id);
    }
}
