package com.example.universityemergencyapp;

import android.content.Intent;
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

    private MapView campusMapView;
    private GoogleMap googleMap;
    private final LatLng campusCenter = new LatLng(23.8315, 78.7810);

    private TextView nearestPointName;
    private TextView nearestPointDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campus_map_resources);

        campusMapView = findViewById(R.id.campusMapView);
        campusMapView.onCreate(savedInstanceState);
        campusMapView.getMapAsync(this);

        EditText searchEditText = findViewById(R.id.searchEditText);
        ImageView resetZoomIcon = findViewById(R.id.resetZoomIcon);
        ChipGroup legendChipGroup = findViewById(R.id.legendChipGroup);
        Chip chipSecurity = findViewById(R.id.chipSecurity);
        Chip chipMedical = findViewById(R.id.chipMedical);
        Chip chipHostel = findViewById(R.id.chipHostel);
        FloatingActionButton sosFab = findViewById(R.id.sosFab);
        FloatingActionButton zoomInFab = findViewById(R.id.zoomInFab);
        FloatingActionButton zoomOutFab = findViewById(R.id.zoomOutFab);
        nearestPointName = findViewById(R.id.nearestPointName);
        nearestPointDistance = findViewById(R.id.nearestPointDistance);
        MaterialButton directionsButton = findViewById(R.id.directionsButton);
        MaterialButton callSecurityButton = findViewById(R.id.callSecurityButton);

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

        sosFab.setOnClickListener(v -> {
            SosRepository.getInstance().triggerSosFromUser(this);
            startActivity(new Intent(this, SosActiveActivity.class));
        });

        zoomInFab.setOnClickListener(v -> {
            if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        });
        zoomOutFab.setOnClickListener(v -> {
            if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        });

        directionsButton.setOnClickListener(v -> centerMap());

        callSecurityButton.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:100"))));

        BottomNavHelper.setup(this, BottomNavHelper.Tab.MAP);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_LAT) && intent.hasExtra(EXTRA_LNG)) {
            double targetLat = intent.getDoubleExtra(EXTRA_LAT, campusCenter.latitude);
            double targetLng = intent.getDoubleExtra(EXTRA_LNG, campusCenter.longitude);
            String title = intent.getStringExtra(EXTRA_TITLE);
            if (title == null) title = "🚨 User Live SOS Location";

            LatLng targetLatLng = new LatLng(targetLat, targetLng);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 17.5f));

            Marker sosMarker = googleMap.addMarker(new MarkerOptions()
                    .position(targetLatLng)
                    .title(title));
            if (sosMarker != null) {
                sosMarker.showInfoWindow();
            }

            if (nearestPointName != null) nearestPointName.setText(title);
            if (nearestPointDistance != null) {
                nearestPointDistance.setText(String.format("Exact Coordinates: Lat %.5f°, Lng %.5f°", targetLat, targetLng));
            }
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusCenter, 16.5f));
        }

        Marker security = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(23.8318, 78.7805))
                .title("Campus Security Office"));

        Marker medical = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(23.8312, 78.7818))
                .title("Health Centre"));

        Marker hostel = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(23.8325, 78.7822))
                .title("Boys Hostel Block C"));

        googleMap.setOnMarkerClickListener(marker -> {
            nearestPointName.setText(marker.getTitle());
            nearestPointDistance.setText("Dr. Harisingh Gour Vishwavidyalaya, Sagar (M.P.)");
            return false; // let the default info-window / camera behaviour still happen
        });
    }

    private void centerMap() {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(campusCenter, 16.5f));
        }
    }

    // ===== MapView lifecycle forwarding =====
    @Override
    protected void onResume() {
        super.onResume();
        campusMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        campusMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        campusMapView.onStop();
    }

    @Override
    protected void onPause() {
        campusMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        campusMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        campusMapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        campusMapView.onSaveInstanceState(outState);
    }
}
