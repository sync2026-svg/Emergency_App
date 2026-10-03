package com.example.universityemergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AlertsActivity extends AppCompatActivity {

    private ChipGroup filterChipGroup;
    private RecyclerView rvAllAlerts;
    private View emptyState;
    private AlertAdapter adapter;

    private List<Alert> allAlerts = new ArrayList<>();   // Store all alerts from Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        filterChipGroup = findViewById(R.id.filterChipGroup);
        rvAllAlerts = findViewById(R.id.rvAllAlerts);
        emptyState = findViewById(R.id.emptyState);

        rvAllAlerts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AlertAdapter(new ArrayList<>(), alert -> {
            Intent intent = new Intent(this, AlertDetailActivity.class);
            intent.putExtra(AlertDetailActivity.EXTRA_ALERT_ID, alert.getId());
            startActivity(intent);
        });
        rvAllAlerts.setAdapter(adapter);

        // Load alerts from Firebase
        loadAlertsFromFirebase();

        // Chip filters
        Chip chipAll = findViewById(R.id.chipAll);
        Chip chipCritical = findViewById(R.id.chipCritical);
        Chip chipWeather = findViewById(R.id.chipWeather);
        Chip chipHostel = findViewById(R.id.chipHostel);
        Chip chipResolved = findViewById(R.id.chipResolved);

        View.OnClickListener chipClick = v -> {
            Chip chip = (Chip) v;
            for (int i = 0; i < filterChipGroup.getChildCount(); i++) {
                View child = filterChipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    ((Chip) child).setChecked(child == chip);
                }
            }
            filterAlerts(chip.getText().toString());
        };

        chipAll.setOnClickListener(chipClick);
        chipCritical.setOnClickListener(chipClick);
        chipWeather.setOnClickListener(chipClick);
        chipHostel.setOnClickListener(chipClick);
        chipResolved.setOnClickListener(chipClick);

        BottomNavHelper.setup(this, BottomNavHelper.Tab.ALERTS);
    }

    private void loadAlertsFromFirebase() {
        DatabaseReference alertsRef = FirebaseDatabase.getInstance()
                .getReference("Admin")
                .child("Campus_Alerts");

        alertsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allAlerts.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String id = dataSnapshot.getKey();
                    String title = dataSnapshot.child("title").getValue(String.class);
                    String shortDesc = dataSnapshot.child("shortDescription").getValue(String.class);
                    String fullBody = dataSnapshot.child("fullBody").getValue(String.class);
                    Long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                    String status = dataSnapshot.child("status").getValue(String.class);

                    String timeAgo = timestamp != null ? getTimeAgo(timestamp) : "Just now";

                    Alert alert = new Alert(
                            id,
                            title != null ? title : "No Title",
                            shortDesc != null ? shortDesc : "",
                            fullBody != null ? fullBody : "",
                            timeAgo,
                            Alert.SEVERITY_CRITICAL,
                            status != null ? status : "Active",
                            "DHSGSU Campus",
                            "Campus Authority"
                    );

                    allAlerts.add(alert);
                }

                // Show all alerts by default
                adapter.submitList(allAlerts);
                updateEmptyState(allAlerts.isEmpty());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AlertsActivity.this, "Failed to load alerts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAlerts(String category) {
        List<Alert> filtered = new ArrayList<>();

        for (Alert alert : allAlerts) {
            if ("All".equalsIgnoreCase(category) ||
                    (alert.getCategory() != null && alert.getCategory().equalsIgnoreCase(category))) {
                filtered.add(alert);
            }
        }

        adapter.submitList(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvAllAlerts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff / 60000) + " min ago";
        if (diff < 86400000) return (diff / 3600000) + " hours ago";
        return (diff / 86400000) + " days ago";
    }
}