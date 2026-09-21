package com.example.universityemergencyapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SosActiveActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView tvElapsedTime;
    private int elapsedSeconds = 0;
    private boolean running = true;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            elapsedSeconds++;
            int minutes = elapsedSeconds / 60;
            int seconds = elapsedSeconds % 60;
            tvElapsedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_active);

        tvElapsedTime = findViewById(R.id.tvElapsedTime);
        ImageButton btnCallResponder = findViewById(R.id.btnCallResponder);
        TextView btnCancelSos = findViewById(R.id.btnCancelSos);

        btnCallResponder.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:100"))));

        btnCancelSos.setOnClickListener(v -> {
            running = false;
            Toast.makeText(this, "SOS cancelled — glad you're safe.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        handler.postDelayed(ticker, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacks(ticker);
    }

    @Override
    public void onBackPressed() {
        // Prevent accidentally leaving an active SOS with the system back button;
        // require using "I'm Safe Now" instead.
        Toast.makeText(this, "Tap \"I'm Safe Now\" to cancel the alert.", Toast.LENGTH_SHORT).show();
    }
}
