package com.example.universityemergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AuthorityAlertsActivity extends AppCompatActivity {

    private EditText etAlertTitle;
    private EditText etAlertLocation;
    private EditText etAlertDesc;

    private RecyclerView rvPublishedAlerts;
    private AlertAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_alerts);

        etAlertTitle = findViewById(R.id.etAlertTitle);
        etAlertLocation = findViewById(R.id.etAlertLocation);
        etAlertDesc = findViewById(R.id.etAlertDesc);
        MaterialButton btnPublishAlert = findViewById(R.id.btnPublishAlert);

        rvPublishedAlerts = findViewById(R.id.rvPublishedAlerts);
        rvPublishedAlerts.setLayoutManager(new LinearLayoutManager(this));

        loadAlerts();

        btnPublishAlert.setOnClickListener(v -> publishAlert());

        AuthorityNavHelper.setup(this, AuthorityNavHelper.Tab.ALERTS);
    }

    private void loadAlerts() {
        List<Alert> alertList = AlertRepository.getAlerts();
        adapter = new AlertAdapter(alertList, alert -> {
            Intent intent = new Intent(this, AlertDetailActivity.class);
            intent.putExtra(AlertDetailActivity.EXTRA_ALERT_ID, alert.getId());
            startActivity(intent);
        });

        adapter.setOnAlertDeleteListener((alert, position) -> showDeleteConfirmationDialog(alert, position));

        rvPublishedAlerts.setAdapter(adapter);
    }

    private void showDeleteConfirmationDialog(Alert alert, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Broadcast Alert")
                .setMessage("Are you sure you want to remove \"" + alert.getTitle() + "\"? It will be removed from all campus devices.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AlertRepository.deleteAlert(alert);
                    adapter.removeAt(position);
                    Toast.makeText(this, "Alert deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void publishAlert() {
        String title = etAlertTitle.getText().toString().trim();
        String location = etAlertLocation.getText().toString().trim();
        String desc = etAlertDesc.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etAlertTitle.setError("Title is required");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            location = "DHSGSU Campus";
        }

        String newId = String.valueOf(System.currentTimeMillis());
        Alert newAlert = new Alert(
                newId,
                title,
                location,
                desc.isEmpty() ? title : desc,
                "Just now",
                Alert.SEVERITY_CRITICAL,
                "Critical",
                location,
                "Campus Authority"
        );

        AlertRepository.getAlerts().add(0, newAlert);
        adapter.notifyItemInserted(0);
        rvPublishedAlerts.scrollToPosition(0);

        etAlertTitle.setText("");
        etAlertLocation.setText("");
        etAlertDesc.setText("");

        Toast.makeText(this, "📢 Emergency alert broadcasted to all campus devices!", Toast.LENGTH_LONG).show();
    }
}
