package com.example.universityemergencyapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SosActiveActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final double USER_LAT = 23.8315;
    private static final double USER_LNG = 78.7810;
    private static final double SECURITY_START_LAT = 23.8385;
    private static final double SECURITY_START_LNG = 78.7875;
    private static final int INITIAL_DISTANCE_METERS = 800;
    private static final int TOTAL_APPROACH_SECONDS = 60;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private MapView sosMapView;
    private GoogleMap googleMap;
    private Marker securityMarker;
    private Polyline routePolyline;

    private TextView tvElapsedTime;
    private TextView tvLiveDistancePill;
    private TextView tvEtaDisplay;
    private TextView tvDistanceDetail;
    private ProgressBar pbDistanceProgress;
    private TextView tvTimelineStatus;

    private int elapsedSeconds = 0;
    private boolean running = true;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            elapsedSeconds++;

            // Format timer 00:00
            int minutes = elapsedSeconds / 60;
            int seconds = elapsedSeconds % 60;
            tvElapsedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

            // Rapido style distance approach calculation
            double progress = Math.min(1.0, (double) elapsedSeconds / TOTAL_APPROACH_SECONDS);
            int currentDistanceMeters = (int) (INITIAL_DISTANCE_METERS * (1.0 - progress));
            int etaMins = Math.max(1, (int) Math.ceil(currentDistanceMeters / 250.0));

            if (currentDistanceMeters > 0) {
                tvEtaDisplay.setText(etaMins + " MINS");
                tvDistanceDetail.setText("Distance: " + currentDistanceMeters + " meters away");
                tvLiveDistancePill.setText(String.format(Locale.US, "📍 Patrol %.2f km away · Approaching", currentDistanceMeters / 1000.0));
                tvTimelineStatus.setText("Patrol Bike #CP-104 is " + currentDistanceMeters + "m away");
            } else {
                tvEtaDisplay.setText("ARRIVED");
                tvDistanceDetail.setText("Security Patrol Arrived at your location!");
                tvLiveDistancePill.setText("🚨 Security Patrol Arrived!");
                tvTimelineStatus.setText("Patrol Officer Vikram Singh arrived at your location");
            }

            pbDistanceProgress.setProgress(INITIAL_DISTANCE_METERS - currentDistanceMeters);

            // Update Map Marker position & Route line
            if (googleMap != null) {
                double currLat = SECURITY_START_LAT + (USER_LAT - SECURITY_START_LAT) * progress;
                double currLng = SECURITY_START_LNG + (USER_LNG - SECURITY_START_LNG) * progress;
                LatLng newSecurityPos = new LatLng(currLat, currLng);

                if (securityMarker != null) {
                    securityMarker.setPosition(newSecurityPos);
                }

                if (routePolyline != null) {
                    List<LatLng> points = new ArrayList<>();
                    points.add(newSecurityPos);
                    points.add(new LatLng(USER_LAT, USER_LNG));
                    routePolyline.setPoints(points);
                }
            }

            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_active);

        tvElapsedTime = findViewById(R.id.tvElapsedTime);
        tvLiveDistancePill = findViewById(R.id.tvLiveDistancePill);
        tvEtaDisplay = findViewById(R.id.tvEtaDisplay);
        tvDistanceDetail = findViewById(R.id.tvDistanceDetail);
        pbDistanceProgress = findViewById(R.id.pbDistanceProgress);
        tvTimelineStatus = findViewById(R.id.tvTimelineStatus);

        sosMapView = findViewById(R.id.sosMapView);
        if (sosMapView != null) {
            sosMapView.onCreate(savedInstanceState);
            sosMapView.getMapAsync(this);
        }

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(SosActiveActivity.this, "Tap \"I'm Safe Now\" to cancel the alert.", Toast.LENGTH_SHORT).show();
            }
        });

        handler.postDelayed(ticker, 1000);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        LatLng userPos = new LatLng(USER_LAT, USER_LNG);
        LatLng securityPos = new LatLng(SECURITY_START_LAT, SECURITY_START_LNG);

        // Add User SOS Location Marker
        googleMap.addMarker(new MarkerOptions()
                .position(userPos)
                .title("You (SOS Location)"));

        // Add Security Patrol Officer Marker with Patrol Bike Icon
        BitmapDescriptor bikeIcon = getBitmapDescriptorFromVector(R.drawable.ic_patrol_bike);

        securityMarker = googleMap.addMarker(new MarkerOptions()
                .position(securityPos)
                .title("Officer Vikram Singh (Patrol Bike #CP-104)")
                .icon(bikeIcon)
                .anchor(0.5f, 0.5f));

        if (securityMarker != null) {
            securityMarker.showInfoWindow();
        }

        // Connect both with a Rapido-style route polyline
        routePolyline = googleMap.addPolyline(new PolylineOptions()
                .add(securityPos, userPos)
                .width(8f)
                .color(Color.parseColor("#D32F2F")));

        // Adjust camera to fit both markers with padding
        try {
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(userPos)
                    .include(securityPos)
                    .build();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPos, 15f));
        }
    }

    private BitmapDescriptor getBitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
        }
        int width = vectorDrawable.getIntrinsicWidth() > 0 ? vectorDrawable.getIntrinsicWidth() : 72;
        int height = vectorDrawable.getIntrinsicHeight() > 0 ? vectorDrawable.getIntrinsicHeight() : 72;
        vectorDrawable.setBounds(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // ===== MapView lifecycle forwarding =====
    @Override
    protected void onResume() {
        super.onResume();
        if (sosMapView != null) sosMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (sosMapView != null) sosMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sosMapView != null) sosMapView.onStop();
    }

    @Override
    protected void onPause() {
        if (sosMapView != null) sosMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        running = false;
        handler.removeCallbacks(ticker);
        if (sosMapView != null) sosMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (sosMapView != null) sosMapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (sosMapView != null) sosMapView.onSaveInstanceState(outState);
    }
}
