package com.example.universityemergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AlertsActivity extends AppCompatActivity {

    private ChipGroup filterChipGroup;
    private RecyclerView rvAllAlerts;
    private View emptyState;
    private AlertAdapter adapter;

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
        adapter = new AlertAdapter(AlertRepository.getAlerts(), alert -> {
            Intent intent = new Intent(this, AlertDetailActivity.class);
            intent.putExtra(AlertDetailActivity.EXTRA_ALERT_ID, alert.getId());
            startActivity(intent);
        });
        rvAllAlerts.setAdapter(adapter);

        Chip chipAll = findViewById(R.id.chipAll);
        Chip chipCritical = findViewById(R.id.chipCritical);
        Chip chipWeather = findViewById(R.id.chipWeather);
        Chip chipHostel = findViewById(R.id.chipHostel);
        Chip chipResolved = findViewById(R.id.chipResolved);

        View.OnClickListener chipClick = v -> {
            Chip chip = (Chip) v;
            // Single-select behaviour: only the tapped chip stays checked.
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

    private void filterAlerts(String category) {
        List<Alert> filtered = new ArrayList<>();
        for (Alert alert : AlertRepository.getAlerts()) {
            if ("All".equalsIgnoreCase(category) || alert.getCategory().equalsIgnoreCase(category)) {
                filtered.add(alert);
            }
        }
        adapter.submitList(filtered);
        boolean isEmpty = filtered.isEmpty();
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvAllAlerts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
