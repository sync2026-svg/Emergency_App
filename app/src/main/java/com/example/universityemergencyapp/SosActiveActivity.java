package com.example.universityemergencyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
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
import androidx.core.app.ActivityCompat;
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

    private static final double SECURITY_START_LAT = 23.826380065526923;
    private static final double SECURITY_START_LNG = 78.77118961928987;
    private static final int TOTAL_APPROACH_SECONDS = 60;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;

    private double userLat = 23.8315;
    private double userLng = 78.7810;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private MapView sosMapView;
    private GoogleMap googleMap;
    private Marker securityMarker;
    private Marker userMarker;
    private Polyline routePolyline;

    private TextView tvElapsedTime;
    private TextView tvLiveDistancePill;
    private TextView tvEtaDisplay;
    private TextView tvDistanceDetail;
    private ProgressBar pbDistanceProgress;
    private TextView tvTimelineStatus;

    private int elapsedSeconds = 0;
    private boolean running = false;
    private int initialDistanceMeters = 1500;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            elapsedSeconds += 2; // Update every 2 seconds

            int minutes = elapsedSeconds / 60;
            int seconds = elapsedSeconds % 60;
            tvElapsedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

            double progress = Math.min(1.0, (double) elapsedSeconds / TOTAL_APPROACH_SECONDS);

            double currLat;
            double currLng;
            if (progress >= 1.0 || elapsedSeconds >= TOTAL_APPROACH_SECONDS) {
                currLat = userLat;
                currLng = userLng;
            } else {
                currLat = SECURITY_START_LAT + (userLat - SECURITY_START_LAT) * progress;
                currLng = SECURITY_START_LNG + (userLng - SECURITY_START_LNG) * progress;
            }
            LatLng currentSecurityPos = new LatLng(currLat, currLng);

            float[] results = new float[1];
            Location.distanceBetween(
                    currentSecurityPos.latitude, currentSecurityPos.longitude,
                    userLat, userLng,
                    results
            );
            int currentDistanceMeters = (int) results[0];

            int etaSeconds = Math.max(1, (int) (currentDistanceMeters / 7.0));
            int etaMins = etaSeconds / 60;
            int etaSecsRem = etaSeconds % 60;

            if (currentDistanceMeters > 20) {
                String etaStr = etaMins > 0 ? etaMins + "m " + etaSecsRem + "s" : etaSecsRem + "s";
                tvEtaDisplay.setText(etaStr.toUpperCase());
                tvDistanceDetail.setText("Distance: " + currentDistanceMeters + " meters away");
                tvLiveDistancePill.setText(String.format(Locale.US, "📍 Patrol %.2f km away · En Route", currentDistanceMeters / 1000.0));
                tvTimelineStatus.setText("Patrol Bike #CP-104 is " + currentDistanceMeters + "m away on road");
            } else {
                tvEtaDisplay.setText("ARRIVED");
                tvDistanceDetail.setText("Security Patrol Arrived at your location!");
                tvLiveDistancePill.setText("🚨 Security Patrol Arrived!");
                tvTimelineStatus.setText("Patrol Officer Vikram Singh arrived at your location");
                currentDistanceMeters = 0;
            }

            if (initialDistanceMeters == 1500 && currentDistanceMeters > 0) {
                initialDistanceMeters = currentDistanceMeters;
                pbDistanceProgress.setMax(initialDistanceMeters);
            }
            pbDistanceProgress.setProgress(Math.max(0, initialDistanceMeters - currentDistanceMeters));

            if (googleMap != null && securityMarker != null) {
                securityMarker.setPosition(currentSecurityPos);
            }

            handler.postDelayed(this, 2000); // Update every 2 seconds
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

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchUserLocationAndStart();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
                fetchUserLocationAndStart();
            } else {
                Toast.makeText(this, "Location permission denied. Using SOS event location.", Toast.LENGTH_SHORT).show();
                fetchUserLocationAndStart();
            }
        }
    }

    private void fetchUserLocationAndStart() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        List<SosEvent> activeSos = SosRepository.getInstance().getActiveSosList();
        if (!activeSos.isEmpty() && userLat == 23.8315 && userLng == 78.7810) {
            SosEvent latestSos = activeSos.get(0);
            userLat = latestSos.getLatitude();
            userLng = latestSos.getLongitude();
        }

        setupMapIfReady();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        setupMapIfReady();
    }

    private void setupMapIfReady() {
        if (googleMap == null || running) return;

        LatLng userPos = new LatLng(userLat, userLng);
        LatLng securityPos = new LatLng(SECURITY_START_LAT, SECURITY_START_LNG);

        // Add User SOS Location Marker
        userMarker = googleMap.addMarker(new MarkerOptions()
                .position(userPos)
                .title("You (Exact SOS Location)"));

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

        // Draw direction view road polyline connecting security to user location
        List<LatLng> roadPoints = new ArrayList<>();
        roadPoints.add(securityPos);
        roadPoints.add(new LatLng((securityPos.latitude + userPos.latitude) / 2.0 + 0.001, (securityPos.longitude + userPos.longitude) / 2.0));
        roadPoints.add(userPos);

        routePolyline = googleMap.addPolyline(new PolylineOptions()
                .addAll(roadPoints)
                .width(10f)
                .color(Color.parseColor("#1976D2")));

        // Enable Google Maps features to show exact present roads, buildings, and traffic naturally
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setTrafficEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setIndoorEnabled(true);

        // Enable smooth pinch-to-zoom and touch gestures without zoom buttons
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);

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

        running = true;
        handler.postDelayed(ticker, 2000); // Update every 2 seconds
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
