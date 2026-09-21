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

/**
 * Displays the DHSGSU campus map with a few sample emergency-point markers.
 * Frontend only: markers are hard-coded locally, nothing is fetched from a backend.
 * NOTE: requires the Google Maps SDK dependency (play-services-maps) and a valid
 * Maps API key declared in AndroidManifest.xml to actually render map tiles.
 */
public class CampusMapActivity extends AppCompatActivity implements OnMapReadyCallback {

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

        sosFab.setOnClickListener(v -> startActivity(new Intent(this, SosConfirmActivity.class)));

        zoomInFab.setOnClickListener(v -> {
            if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        });
        zoomOutFab.setOnClickListener(v -> {
            if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        });

        directionsButton.setOnClickListener(v -> centerMap());

        callSecurityButton.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:100"))));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusCenter, 16.5f));

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
