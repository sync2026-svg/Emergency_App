package com.example.universityemergencyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CampusMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LNG = "extra_lng";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_IS_PICKER = "extra_is_picker";
    public static final String EXTRA_SELECTED_TITLE = "extra_selected_title";
    public static final String EXTRA_SELECTED_LAT = "extra_selected_lat";
    public static final String EXTRA_SELECTED_LNG = "extra_selected_lng";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView campusMapView;
    private GoogleMap googleMap;
    private final LatLng campusCenter = new LatLng(23.8315, 78.7810);

    private TextView nearestPointName;
    private TextView nearestPointDistance;
    private boolean isPicker = false;
    private String currentSelectedTitle = "Dr. Harisingh Gour Vishwavidyalaya Campus";
    private double currentSelectedLat = 23.8315;
    private double currentSelectedLng = 78.7810;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campus_map_resources);

        campusMapView = findViewById(R.id.campusMapView);
        if (campusMapView != null) {
            campusMapView.onCreate(savedInstanceState);
            campusMapView.getMapAsync(this);
        }

        EditText searchEditText = findViewById(R.id.searchEditText);
        ImageView resetZoomIcon = findViewById(R.id.resetZoomIcon);
        ChipGroup legendChipGroup = findViewById(R.id.legendChipGroup);
        Chip chipSecurity = findViewById(R.id.chipSecurity);
        Chip chipMedical = findViewById(R.id.chipMedical);
        Chip chipHostel = findViewById(R.id.chipHostel);
        FloatingActionButton zoomInFab = findViewById(R.id.zoomInFab);
        FloatingActionButton zoomOutFab = findViewById(R.id.zoomOutFab);
        nearestPointName = findViewById(R.id.nearestPointName);
        nearestPointDistance = findViewById(R.id.nearestPointDistance);
        MaterialButton directionsButton = findViewById(R.id.directionsButton);
        MaterialButton callSecurityButton = findViewById(R.id.callSecurityButton);

        isPicker = getIntent().getBooleanExtra(EXTRA_IS_PICKER, false);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    Toast.makeText(this, "Searching for \"" + query + "\"…", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        resetZoomIcon.setOnClickListener(v -> centerMap());

        chipSecurity.setOnClickListener(v -> Toast.makeText(this, "Showing Security points", Toast.LENGTH_SHORT).show());
        chipMedical.setOnClickListener(v -> Toast.makeText(this, "Showing Medical points", Toast.LENGTH_SHORT).show());
        chipHostel.setOnClickListener(v -> Toast.makeText(this, "Showing Hostels", Toast.LENGTH_SHORT).show());


        zoomInFab.setOnClickListener(v -> {
            if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        });
        zoomOutFab.setOnClickListener(v -> {
            if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        });

        if (isPicker) {
            directionsButton.setText("Confirm Location");
            directionsButton.setOnClickListener(v -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_TITLE, currentSelectedTitle);
                resultIntent.putExtra(EXTRA_SELECTED_LAT, currentSelectedLat);
                resultIntent.putExtra(EXTRA_SELECTED_LNG, currentSelectedLng);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
            Toast.makeText(this, "Tap a campus marker to select location", Toast.LENGTH_LONG).show();
        } else {
            directionsButton.setOnClickListener(v -> centerMap());
        }

        callSecurityButton.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:100"))));

        BottomNavHelper.setup(this, BottomNavHelper.Tab.MAP);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        Intent intent = getIntent();
        LatLng targetLatLng = campusCenter;
        String title = "Dr. Harisingh Gour Vishwavidyalaya Campus";

        if (intent != null && intent.hasExtra(EXTRA_LAT) && intent.hasExtra(EXTRA_LNG)) {
            double targetLat = intent.getDoubleExtra(EXTRA_LAT, campusCenter.latitude);
            double targetLng = intent.getDoubleExtra(EXTRA_LNG, campusCenter.longitude);
            String intentTitle = intent.getStringExtra(EXTRA_TITLE);
            if (intentTitle != null) title = intentTitle;
            else title = "🚨 User Live SOS Location";

            targetLatLng = new LatLng(targetLat, targetLng);
            currentSelectedTitle = title;
            currentSelectedLat = targetLat;
            currentSelectedLng = targetLng;

            if (nearestPointName != null) nearestPointName.setText(title);
            if (nearestPointDistance != null) {
                nearestPointDistance.setText(String.format("Exact Coordinates: Lat %.5f°, Lng %.5f°", targetLat, targetLng));
            }
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 17f));

        // Add Target Marker
        Marker targetMarker = googleMap.addMarker(new MarkerOptions().position(targetLatLng).title(title));
        if (targetMarker != null) targetMarker.showInfoWindow();

        // Add standard campus markers
        addCampusMarker(new LatLng(23.8318, 78.7805), "Campus Security Office");
        addCampusMarker(new LatLng(23.8312, 78.7818), "Health Centre");
        addCampusMarker(new LatLng(23.8325, 78.7822), "Boys Hostel Block C");

        googleMap.setOnMarkerClickListener(marker -> {
            currentSelectedTitle = marker.getTitle();
            currentSelectedLat = marker.getPosition().latitude;
            currentSelectedLng = marker.getPosition().longitude;
            if (nearestPointName != null) nearestPointName.setText(marker.getTitle());
            if (nearestPointDistance != null) {
                nearestPointDistance.setText("Dr. Harisingh Gour Vishwavidyalaya, Sagar (M.P.)");
            }
            marker.showInfoWindow();
            return false;
        });

        checkLocationPermission();
    }

    private void addCampusMarker(LatLng latLng, String title) {
        googleMap.addMarker(new MarkerOptions().position(latLng).title(title));
    }

    private void centerMap() {
        if (googleMap != null) {
            currentSelectedTitle = "Dr. Harisingh Gour Vishwavidyalaya Campus";
            currentSelectedLat = campusCenter.latitude;
            currentSelectedLng = campusCenter.longitude;
            if (nearestPointName != null) nearestPointName.setText(currentSelectedTitle);
            if (nearestPointDistance != null) nearestPointDistance.setText("Sagar (M.P.)");
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(campusCenter, 16.5f));
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocationLayer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
                enableMyLocationLayer();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enableMyLocationLayer() {
        try {
            if (googleMap != null && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                googleMap.setMyLocationEnabled(true);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // ===== MapView lifecycle forwarding =====
    @Override
    protected void onResume() {
        super.onResume();
        if (campusMapView != null) campusMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (campusMapView != null) campusMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (campusMapView != null) campusMapView.onStop();
    }

    @Override
    protected void onPause() {
        if (campusMapView != null) campusMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (campusMapView != null) campusMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (campusMapView != null) campusMapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (campusMapView != null) campusMapView.onSaveInstanceState(outState);
    }
}
