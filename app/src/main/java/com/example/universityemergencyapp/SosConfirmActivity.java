package com.example.universityemergencyapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SosConfirmActivity extends AppCompatActivity {

    private static final long HOLD_DURATION_MS = 3000;
    private static final long TICK_MS = 30;

    private ProgressBar holdProgress;
    private CountDownTimer holdTimer;
    private boolean sosSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_confirm);

        ImageButton btnCloseSos = findViewById(R.id.btnCloseSos);
        TextView btnHoldToSend = findViewById(R.id.btnHoldToSend);
        holdProgress = findViewById(R.id.holdProgress);
        LinearLayout btnCallSecurityDirect = findViewById(R.id.btnCallSecurityDirect);
        LinearLayout btnCallAmbulanceDirect = findViewById(R.id.btnCallAmbulanceDirect);
        LinearLayout btnCallPoliceDirect = findViewById(R.id.btnCallPoliceDirect);

        btnCloseSos.setOnClickListener(v -> finish());

        btnHoldToSend.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startHold();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    cancelHold();
                    return true;
            }
            return false;
        });

        btnCallSecurityDirect.setOnClickListener(v -> dial("100"));
        btnCallAmbulanceDirect.setOnClickListener(v -> dial("108"));
        btnCallPoliceDirect.setOnClickListener(v -> dial("100"));
    }

    private void startHold() {
        if (sosSent) return;
        cancelHold();
        holdTimer = new CountDownTimer(HOLD_DURATION_MS, TICK_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsed = HOLD_DURATION_MS - millisUntilFinished;
                int progress = (int) ((elapsed * 100) / HOLD_DURATION_MS);
                holdProgress.setProgress(progress);
            }

            @Override
            public void onFinish() {
                holdProgress.setProgress(100);
                sendSos();
            }
        };
        holdTimer.start();
    }

    private void cancelHold() {
        if (holdTimer != null) {
            holdTimer.cancel();
            holdTimer = null;
        }
        if (!sosSent) {
            holdProgress.setProgress(0);
        }
    }

    private void sendSos() {
        sosSent = true;
        startActivity(new Intent(this, SosActiveActivity.class));
        finish();
    }

    private void dial(String number) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (holdTimer != null) {
            holdTimer.cancel();
        }
    }
}
