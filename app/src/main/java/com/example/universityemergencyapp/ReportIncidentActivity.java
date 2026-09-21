package com.example.universityemergencyapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;

public class ReportIncidentActivity extends AppCompatActivity {

    private String selectedLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ChipGroup categoryChipGroup = findViewById(R.id.categoryChipGroup);
        LinearLayout layoutLocation = findViewById(R.id.layoutLocation);
        TextView tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        EditText etDescription = findViewById(R.id.etDescription);
        LinearLayout btnAddPhoto = findViewById(R.id.btnAddPhoto);
        Switch switchAnonymous = findViewById(R.id.switchAnonymous);
        TextView btnSubmitReport = findViewById(R.id.btnSubmitReport);

        btnBack.setOnClickListener(v -> finish());

        // Tapping the location field opens the campus map to pick a spot.
        layoutLocation.setOnClickListener(v -> {
            selectedLocation = "Pinned near current location";
            tvSelectedLocation.setText(selectedLocation);
            tvSelectedLocation.setTextColor(getResources().getColor(R.color.text_primary));
            Toast.makeText(this, "Location pinned", Toast.LENGTH_SHORT).show();
        });

        btnAddPhoto.setOnClickListener(v ->
                Toast.makeText(this, "Photo attachment coming soon", Toast.LENGTH_SHORT).show());

        btnSubmitReport.setOnClickListener(v -> {
            int checkedChipId = categoryChipGroup.getCheckedChipId();
            if (checkedChipId == -1) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }
            String description = etDescription.getText().toString().trim();
            if (description.isEmpty()) {
                etDescription.setError("Please describe what happened");
                etDescription.requestFocus();
                return;
            }

            boolean anonymous = switchAnonymous.isChecked();
            // Frontend-only: nothing is sent to a backend. Just confirm to the user.
            String msg = anonymous
                    ? "Report submitted anonymously. Thank you."
                    : "Report submitted. Campus Security will follow up if needed.";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
