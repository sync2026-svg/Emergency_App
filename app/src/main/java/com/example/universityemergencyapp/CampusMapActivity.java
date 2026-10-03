package com.example.universityemergencyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import android.graphics.Color;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import java.util.Arrays;
import com.google.android.gms.location.FusedLocationProviderClient;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.content.IntentSender;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.LocationServices;
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
    private static final int REQUEST_CHECK_SETTINGS = 1002;

    private MapView campusMapView;
    private GoogleMap googleMap;
    private final LatLng campusCenter = new LatLng(23.826020153828477, 78.77144340109342);

    private TextView nearestPointName;
    private TextView nearestPointDistance;
    private boolean isPicker = false;
    private String currentSelectedTitle = "Dr. Harisingh Gour Vishwavidyalaya Campus";
    private double currentSelectedLat = 23.8315;
    private double currentSelectedLng = 78.7810;

    private Polyline currentRoutePath;
    private LatLng userCurrentLocation;

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
        MaterialButton showLocationButton = findViewById(R.id.showLocationButton);

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
            directionsButton.setText(R.string.confirm_location);
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
            directionsButton.setText("Show Route");
            directionsButton.setOnClickListener(v -> {
                drawRouteToMarker(new LatLng(currentSelectedLat, currentSelectedLng));
            });
        }

        showLocationButton.setOnClickListener(v -> fetchAndShowCurrentLocation(true));

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
                nearestPointDistance.setText(String.format(getString(R.string.exact_coordinates_lat_5f_lng_5f), targetLat, targetLng));
            }
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 17f));

        // Add Target Marker
        Marker targetMarker = googleMap.addMarker(new MarkerOptions().position(targetLatLng).title(title));
        if (targetMarker != null) targetMarker.showInfoWindow();

        // Add standard campus markers
        populateCampusMarkers();

        googleMap.setOnMarkerClickListener(marker -> {
            currentSelectedTitle = marker.getTitle();
            currentSelectedLat = marker.getPosition().latitude;
            currentSelectedLng = marker.getPosition().longitude;
            if (nearestPointName != null) nearestPointName.setText(marker.getTitle());
            if (nearestPointDistance != null) {
                nearestPointDistance.setText(R.string.dr_harisingh_gour_vishwavidyalaya_sagar_m_p);
            }
            marker.showInfoWindow();
            drawRouteToMarker(marker.getPosition());
            return false;
        });

        checkLocationPermission();
    }

    private void populateCampusMarkers() {
        addCampusMarker(new LatLng(23.82857513356486, 78.77110159555725), "Gour Statue");
        addCampusMarker(new LatLng(23.82695075396646, 78.77099507749111), "Jawaharlal Nehru Central Library");
        addCampusMarker(new LatLng(23.82940220070068, 78.77927525904198), "Saraswati Girl's Hostel");
        addCampusMarker(new LatLng(23.829862087512637, 78.78034077926334), "Nivedita Girl's Hostel");
        addCampusMarker(new LatLng(23.830836767632295, 78.78190904491457), "Rani Laxmi Bai Girl's Hostel");
        addCampusMarker(new LatLng(23.828752251383733, 78.77933604497593), "University Post Office");
        addCampusMarker(new LatLng(23.82463772857434, 78.77489164413385), "University Hospital");
        addCampusMarker(new LatLng(23.821600732661416, 78.77119033053117), "Tagore Boy's Hostel");
        addCampusMarker(new LatLng(23.822236093258105, 78.7729137964336), "Aaryabhatt Boy's Hostel");
        addCampusMarker(new LatLng(23.8207800539331, 78.77021605785184), "Vivekanand Boy's Hostel");
        addCampusMarker(new LatLng(23.81838968749849, 78.76735628712642), "Raman Boy's Hostel");
        addCampusMarker(new LatLng(23.817585048969917, 78.7666964588123), "Bhaba Boy's Hostel");
        addCampusMarker(new LatLng(23.824895958944854, 78.76589903589709), "University Stadium");
        addCampusMarker(new LatLng(23.825358597002197, 78.76486917443279), "University GYM  ");
        addCampusMarker(new LatLng(23.824218388919657, 78.78218618010258), "Dept. of Computer Science and Application");
        addCampusMarker(new LatLng(23.8252580310079, 78.77201148494365),"Abhimanch Sabhaghar");
        addCampusMarker(new LatLng(23.82784015415752, 78.77132593605054),"Gour Prangan");
        addCampusMarker(new LatLng(23.82762317765343, 78.77094914499136),"Gour Samadhi");
    }

    private void addCampusMarker(LatLng latLng, String title) {
        googleMap.addMarker(new MarkerOptions().position(latLng).title(title));
    }

    private void drawRouteToMarker(LatLng destination) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required to draw path.", Toast.LENGTH_SHORT).show();
            return;
        }
        checkDeviceLocationSettings(() -> {
            try {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> {
                    if (location != null && googleMap != null) {
                        userCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        fetchAndDrawRoute(userCurrentLocation, destination);
                    } else {
                        Toast.makeText(this, "Unable to get current location for path.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        });
    }

    private void drawFallbackDottedLine(LatLng origin, LatLng destination, String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if (currentRoutePath != null) {
                currentRoutePath.remove();
            }
            List<PatternItem> pattern = Arrays.asList(new Dot(), new Gap(20f));
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(origin, destination)
                    .width(12f)
                    .color(Color.BLUE)
                    .pattern(pattern)
                    .geodesic(true);
            currentRoutePath = googleMap.addPolyline(polylineOptions);
            Toast.makeText(CampusMapActivity.this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchAndDrawRoute(LatLng origin, LatLng destination) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
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
                // The OSRM public API requires a valid User-Agent header, otherwise it may block the request
                conn.setRequestProperty("User-Agent", "UniversityEmergencyApp/1.0");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP response code: " + responseCode);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                if (!jsonObject.has("routes") || jsonObject.getJSONArray("routes").length() == 0) {
                    throw new Exception("No routes found in the response");
                }

                String polyline = jsonObject.getJSONArray("routes").getJSONObject(0).getString("geometry");
                List<LatLng> decodedPath = decodePolyline(polyline);

                handler.post(() -> {
                    if (currentRoutePath != null) {
                        currentRoutePath.remove();
                    }
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(decodedPath)
                            .width(12f)
                            .color(Color.BLUE)
                            .geodesic(true);
                    currentRoutePath = googleMap.addPolyline(polylineOptions);
                });
            } catch (Exception e) {
                Log.e("CampusMapActivity", "Error fetching route", e);
                drawFallbackDottedLine(origin, destination, "Showing direct path (Road route unavailable)");
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

    private void centerMap() {
        if (googleMap != null) {
            currentSelectedTitle = "Dr. Harisingh Gour Vishwavidyalaya Campus";
            currentSelectedLat = campusCenter.latitude;
            currentSelectedLng = campusCenter.longitude;
            if (nearestPointName != null) nearestPointName.setText(currentSelectedTitle);
            if (nearestPointDistance != null) nearestPointDistance.setText(R.string.sagar_m_p);
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
            checkDeviceLocationSettings(this::enableMyLocationLayer);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
                checkDeviceLocationSettings(this::enableMyLocationLayer);
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
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
                    resolvable.startResolutionForResult(CampusMapActivity.this, REQUEST_CHECK_SETTINGS);
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
                enableMyLocationLayer();
            } else {
                Toast.makeText(this, "GPS is required for full functionality.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchAndShowCurrentLocation(boolean forceCenter) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
            return;
        }

        checkDeviceLocationSettings(() -> {
            try {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> {
                    if (location != null && googleMap != null) {
                        Intent intent = getIntent();
                        boolean hasTarget = intent != null && (intent.hasExtra(EXTRA_LAT) || intent.hasExtra(EXTRA_LNG));

                        if (forceCenter || !hasTarget) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 19.5f));

                            currentSelectedLat = location.getLatitude();
                            currentSelectedLng = location.getLongitude();
                            currentSelectedTitle = "My Current Location";

                            if (nearestPointName != null) nearestPointName.setText(currentSelectedTitle);
                            if (nearestPointDistance != null) {
                                nearestPointDistance.setText(String.format(getString(R.string.exact_coordinates_lat_5f_lng_5f), location.getLatitude(), location.getLongitude()));
                            }
                        }
                    } else if (forceCenter) {
                        Toast.makeText(this, "Unable to get current location. Ensure location services are turned on.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        });
    }

    private void enableMyLocationLayer() {
        try {
            if (googleMap != null && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                googleMap.setMyLocationEnabled(true);
                fetchAndShowCurrentLocation(false);
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
