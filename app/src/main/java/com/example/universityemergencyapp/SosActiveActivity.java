package com.example.universityemergencyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
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

import androidx.core.widget.NestedScrollView;
import android.view.MotionEvent;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.util.Log;
import android.telephony.SmsManager;
import android.content.IntentSender;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

public class SosActiveActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final double SECURITY_START_LAT = 23.826380065526923;
    private static final double SECURITY_START_LNG = 78.77118961928987;
    private int totalApproachSeconds = 60;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;
    private static final int REQUEST_CHECK_SETTINGS = 2002;
    private static final int SMS_PERMISSION_REQUEST_CODE = 3001;

    public double userLat = 0.0;
    public double userLng = 0.0;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private MapView sosMapView;
    private GoogleMap googleMap;
    private Marker securityMarker;
    private Marker userMarker;
    private Polyline routePolyline;

    private List<LatLng> realRoutePoints = null;
    private double routeTotalDistance = 0;

    private TextView tvLiveDistancePill;
    private TextView tvEtaDisplay;
    private TextView tvDistanceDetail;
    private ProgressBar pbDistanceProgress;
    private TextView tvTimelineStatus;

    private long animationStartTime = 0;
    private boolean running = false;
    private int initialDistanceMeters = 1500;


    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            if (animationStartTime == 0) {
                animationStartTime = System.currentTimeMillis();
            }

            float elapsedSeconds = (System.currentTimeMillis() - animationStartTime) / 1000f;
            double progress = Math.min(1.0, (double) elapsedSeconds / totalApproachSeconds);

            LatLng currentSecurityPos;
            int currentDistanceMeters;

            if (progress >= 1.0 || elapsedSeconds >= totalApproachSeconds) {
                currentSecurityPos = new LatLng(userLat, userLng);
                currentDistanceMeters = 0;
            } else if (realRoutePoints != null && realRoutePoints.size() > 1) {
                double targetDist = progress * routeTotalDistance;
                currentSecurityPos = getPointAlongPath(realRoutePoints, targetDist);
                currentDistanceMeters = Math.max(0, (int) (routeTotalDistance - targetDist));
            } else {
                double currLat = SECURITY_START_LAT + (userLat - SECURITY_START_LAT) * progress;
                double currLng = SECURITY_START_LNG + (userLng - SECURITY_START_LNG) * progress;
                currentSecurityPos = new LatLng(currLat, currLng);

                float[] results = new float[1];
                Location.distanceBetween(
                        currentSecurityPos.latitude, currentSecurityPos.longitude,
                        userLat, userLng,
                        results
                );
                currentDistanceMeters = (int) results[0];
            }

            int etaSeconds;
            if (routeTotalDistance > 0 && totalApproachSeconds > 0) {
                double speed = routeTotalDistance / totalApproachSeconds;
                etaSeconds = (int) (currentDistanceMeters / speed);
            } else {
                etaSeconds = Math.max(1, totalApproachSeconds - (int) elapsedSeconds);
            }
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
                LatLng prevPos = securityMarker.getPosition();
                if (prevPos != null && (prevPos.latitude != currentSecurityPos.latitude || prevPos.longitude != currentSecurityPos.longitude)) {
                    float[] bearingRes = new float[2];
                    Location.distanceBetween(prevPos.latitude, prevPos.longitude,
                            currentSecurityPos.latitude, currentSecurityPos.longitude, bearingRes);
                    if (bearingRes[0] > 0.1f) {
                        securityMarker.setRotation(bearingRes[1]);
                    }
                }
                securityMarker.setPosition(currentSecurityPos);
            }

            handler.postDelayed(this, 50); // 50ms delay for ultra smooth 20fps animation
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_active);

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

        findViewById(R.id.btnZoomIn).setOnClickListener(v -> {
            if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        });
        findViewById(R.id.btnZoomOut).setOnClickListener(v -> {
            if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        });
        findViewById(R.id.btnCenterMap).setOnClickListener(v -> {
            if (googleMap != null && userLat != 0.0) {
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(new LatLng(userLat, userLng))
                        .include(securityMarker != null ? securityMarker.getPosition() : new LatLng(SECURITY_START_LAT, SECURITY_START_LNG))
                        .build();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        });

        btnCallResponder.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:100"))));

        btnCancelSos.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
            } else {
                sendSafeSmsAndGoHome();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(SosActiveActivity.this, "Tap \"I'm Safe Now\" to cancel the alert.", Toast.LENGTH_SHORT).show();
            }
        });

        checkLocationPermission();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (sosMapView != null) {
            int[] location = new int[2];
            sosMapView.getLocationOnScreen(location);
            float x = ev.getRawX();
            float y = ev.getRawY();

            // Check if touch is directly on the map
            if (x >= location[0] && x <= location[0] + sosMapView.getWidth() &&
                    y >= location[1] && y <= location[1] + sosMapView.getHeight()) {

                NestedScrollView scrollView = findViewById(R.id.nestedScrollView);
                if (scrollView != null) {
                    // Tell the scroll view NOT to intercept touches meant for the map
                    scrollView.requestDisallowInterceptTouchEvent(true);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkDeviceLocationSettings(this::fetchExactLocationAndStart);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
                checkDeviceLocationSettings(this::fetchExactLocationAndStart);
            } else {
                Toast.makeText(this, "Location permission denied. Cannot start live tracking.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSafeSmsAndGoHome();
            } else {
                Toast.makeText(this, "SMS permission denied. SOS cancelled without sending message.", Toast.LENGTH_SHORT).show();
                goHome();
            }
        }
    }

    private void sendSafeSmsAndGoHome() {
        running = false;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String securityNumber = "7828062947"; // Actual campus security control room number
            String message = "I am safe now. Thank you to the Campus Security team for your rapid response!";
            smsManager.sendTextMessage(securityNumber, null, message, null, null);
            Toast.makeText(this, "Thank you note sent to Security via SMS. Glad you're safe!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS.", Toast.LENGTH_SHORT).show();
        }
        goHome();
    }

    private void goHome() {
        running = false;
        Intent intent = new Intent(this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkDeviceLocationSettings(Runnable onSuccess) {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            if (onSuccess != null) onSuccess.run();
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(SosActiveActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                fetchExactLocationAndStart();
            } else {
                Toast.makeText(this, "GPS is required to get your exact location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchExactLocationAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                   // new Home().getlati(userLat);
                   // new Home().getLongi(userLng);
                    setupMapIfReady();
                } else {
                    Toast.makeText(this, "Could not fetch current live location.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to get location.", Toast.LENGTH_SHORT).show();
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        setupMapIfReady();
    }

    private void setupMapIfReady() {
        if (googleMap == null || running || userLat == 0.0 || userLng == 0.0) return;

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
                .anchor(0.5f, 0.5f)
                .flat(true)); // Flat allows the marker to rotate with the map direction

        if (securityMarker != null) {
            securityMarker.showInfoWindow();
        }

        // Fetch and draw exact real road directions using OSRM
        fetchAndDrawRoute(securityPos, userPos);

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

    private LatLng getPointAlongPath(List<LatLng> path, double targetDistance) {
        double currentDist = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            LatLng p1 = path.get(i);
            LatLng p2 = path.get(i + 1);
            float[] res = new float[1];
            Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, res);
            double segDist = res[0];
            if (currentDist + segDist >= targetDistance) {
                double ratio = (targetDistance - currentDist) / segDist;
                double lat = p1.latitude + (p2.latitude - p1.latitude) * ratio;
                double lng = p1.longitude + (p2.longitude - p1.longitude) * ratio;
                return new LatLng(lat, lng);
            }
            currentDist += segDist;
        }
        return path.get(path.size() - 1);
    }

    private void fetchAndDrawRoute(LatLng origin, LatLng destination) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Using 'walking' profile instead of 'driving' ensures it finds the shortest
                // possible route through campus pathways and internal roads, ignoring car-only one-ways.
                String urlString = "https://router.project-osrm.org/route/v1/walking/" +
                        origin.longitude + "," + origin.latitude + ";" +
                        destination.longitude + "," + destination.latitude +
                        "?overview=full&geometries=polyline";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "UniversityEmergencyApp/1.0");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP response code: " + conn.getResponseCode());
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                if (!jsonObject.has("routes") || jsonObject.getJSONArray("routes").length() == 0) return;

                JSONObject route = jsonObject.getJSONArray("routes").getJSONObject(0);
                String polyline = route.getString("geometry");
                double distance = route.getDouble("distance");
                int duration = route.getInt("duration");

                List<LatLng> decodedPath = decodePolyline(polyline);

                handler.post(() -> {
                    if (routePolyline != null) routePolyline.remove();
                    routePolyline = googleMap.addPolyline(new PolylineOptions()
                            .addAll(decodedPath)
                            .width(12f)
                            .color(Color.BLUE)
                            .geodesic(true));

                    realRoutePoints = decodedPath;
                    routeTotalDistance = distance;

                    // Use exact time from real OSRM calculated route without fake demonstration caps
                    totalApproachSeconds = Math.max(duration, 1);
                    initialDistanceMeters = (int) distance;
                    pbDistanceProgress.setMax(initialDistanceMeters);

                    // Reset animation timer so the real duration applies properly
                    animationStartTime = System.currentTimeMillis();
                });
            } catch (Exception e) {
                Log.e("SosActiveActivity", "Error fetching route", e);
            }
        });
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            poly.add(new LatLng((double) lat / 1E5, (double) lng / 1E5));
        }
        return poly;
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
