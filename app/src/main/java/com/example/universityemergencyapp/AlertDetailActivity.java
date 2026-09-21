package com.example.universityemergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AlertDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ALERT_ID = "extra_alert_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_details);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvSeverityBadge = findViewById(R.id.tvSeverityBadge);
        TextView tvAlertTitle = findViewById(R.id.tvAlertTitle);
        TextView tvAlertMeta = findViewById(R.id.tvAlertMeta);
        TextView tvAlertBody = findViewById(R.id.tvAlertBody);
        TextView btnMarkSafe = findViewById(R.id.btnMarkSafe);
        TextView btnShareAlert = findViewById(R.id.btnShareAlert);

        btnBack.setOnClickListener(v -> finish());

        String alertId = getIntent().getStringExtra(EXTRA_ALERT_ID);
        Alert alert = alertId != null ? AlertRepository.getAlertById(alertId) : null;

        if (alert != null) {
            tvSeverityBadge.setText(alert.getSeverity());
            tvAlertTitle.setText(alert.getTitle());
            tvAlertMeta.setText("Posted by " + alert.getPostedBy() + " · " + alert.getTime());
            tvAlertBody.setText(alert.getFullBody());
        }
        // If no alert id was passed, the layout's sample/design-time text is left as-is.

        btnMarkSafe.setOnClickListener(v ->
                Toast.makeText(this, "You've been marked as safe.", Toast.LENGTH_SHORT).show());

        btnShareAlert.setOnClickListener(v -> {
            String shareText = (alert != null ? alert.getTitle() + "\n\n" + alert.getFullBody() : tvAlertTitle.getText().toString());
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share alert via"));
        });
    }
}
