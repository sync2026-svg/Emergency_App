package com.example.universityemergencyapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class AuthorityDashboardActivity extends AppCompatActivity implements SosRepository.OnSosListener {

    private TextView tvSosUser;
    private TextView tvSosLocation;
    private TextView tvSosTime;

    private Spinner spinnerSelectSecurity;
    private MaterialButton btnDispatchSelectedSecurity;

    private EditText etAlertTitle;
    private EditText etAlertLocation;
    private EditText etAlertDesc;

    private TextView tvDirectoryUserName;
    private TextView tvDirectoryDetails;

    private SosEvent latestSosEvent;
    private Dialog activeSosDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_dashboard);

        tvSosUser = findViewById(R.id.tvSosUser);
        tvSosLocation = findViewById(R.id.tvSosLocation);
        tvSosTime = findViewById(R.id.tvSosTime);

        spinnerSelectSecurity = findViewById(R.id.spinnerSelectSecurity);
        btnDispatchSelectedSecurity = findViewById(R.id.btnDispatchSelectedSecurity);

        MaterialButton btnViewSosPopUp = findViewById(R.id.btnViewSosPopUp);

        etAlertTitle = findViewById(R.id.etAlertTitle);
        etAlertLocation = findViewById(R.id.etAlertLocation);
        etAlertDesc = findViewById(R.id.etAlertDesc);
        MaterialButton btnPublishAlert = findViewById(R.id.btnPublishAlert);

        tvDirectoryUserName = findViewById(R.id.tvDirectoryUserName);
        tvDirectoryDetails = findViewById(R.id.tvDirectoryDetails);

        SosRepository.getInstance().addListener(this);

        setupSecuritySpinner();

        btnViewSosPopUp.setOnClickListener(v -> {
            if (latestSosEvent != null) {
                showSosPopUpWindow(latestSosEvent);
            } else {
                Toast.makeText(this, "No active SOS events found", Toast.LENGTH_SHORT).show();
            }
        });

        btnDispatchSelectedSecurity.setOnClickListener(v -> dispatchSelectedOfficer());

        btnPublishAlert.setOnClickListener(v -> publishCampusAlert());

        refreshData();

        AuthorityNavHelper.setup(this, AuthorityNavHelper.Tab.DASHBOARD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SosRepository.getInstance().removeListener(this);
    }

    @Override
    public void onSosTriggered(SosEvent event) {
        runOnUiThread(() -> {
            this.latestSosEvent = event;
            updateSosCard(event);
            showSosPopUpWindow(event);
        });
    }

    private void setupSecuritySpinner() {
        if (spinnerSelectSecurity == null) return;
        List<String> guardList = SecurityRepository.getInstance(this).getSecurityNamesList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, guardList);
        spinnerSelectSecurity.setAdapter(adapter);
    }

    private void dispatchSelectedOfficer() {
        String officer = "Officer Vikram Singh (CP-104)";
        if (spinnerSelectSecurity != null && spinnerSelectSecurity.getSelectedItem() != null) {
            officer = spinnerSelectSecurity.getSelectedItem().toString();
        }

        if (latestSosEvent != null) {
            latestSosEvent.setStatus("DISPATCHED");
            Toast.makeText(this, "🚨 Dispatched " + officer + " to " + latestSosEvent.getUserName() + "'s location!", Toast.LENGTH_LONG).show();
            refreshData();
        } else {
            Toast.makeText(this, "🚨 Dispatched " + officer + " to active incident location!", Toast.LENGTH_LONG).show();
        }
    }

    private void refreshData() {
        setupSecuritySpinner();
        List<SosEvent> activeList = SosRepository.getInstance().getActiveSosList();
        if (!activeList.isEmpty()) {
            latestSosEvent = activeList.get(0);
            updateSosCard(latestSosEvent);
        }
        loadDirectoryInfo();
    }

    private void updateSosCard(SosEvent event) {
        if (event == null) return;
        tvSosUser.setText(String.format(Locale.getDefault(), "%s (ID: %s)", event.getUserName(), event.getUserId()));
        tvSosLocation.setText(String.format(Locale.getDefault(), "📍 %s (Lat: %.4f, Lng: %.4f)", event.getLocationLandmark(), event.getLatitude(), event.getLongitude()));
        tvSosTime.setText(event.getTimestamp());
    }

    private void loadDirectoryInfo() {
        SharedPreferences prefs = getSharedPreferences(EditProfileActivity.PREFS_NAME, MODE_PRIVATE);

        String name = prefs.getString(EditProfileActivity.KEY_USER_NAME, "Aditi Sharma");
        String id = prefs.getString(EditProfileActivity.KEY_USER_ID, "DHSGSU2026041");
        String phone = prefs.getString(EditProfileActivity.KEY_USER_PHONE, "+91 98765 43210");
        String dept = prefs.getString(EditProfileActivity.KEY_USER_DEPARTMENT, "Computer Science & Applications");
        String blood = prefs.getString(MedicalInfoActivity.KEY_BLOOD_GROUP, "O+");
        String medical = prefs.getString(MedicalInfoActivity.KEY_CONDITIONS, "Asthma (Mild)");

        String pName = prefs.getString(EmergencyContactsActivity.KEY_PRIMARY_NAME, "Dr. Rajesh Sharma");
        String pPhone = prefs.getString(EmergencyContactsActivity.KEY_PRIMARY_PHONE, "+91 98765 12345");

        tvDirectoryUserName.setText(name);
        String details = String.format(Locale.getDefault(),
                "ID: %s · %s\nPhone: %s\nBlood Group: %s · Medical: %s\nPrimary Contact: %s (%s)",
                id, dept, phone, blood, medical, pName, pPhone);
        tvDirectoryDetails.setText(details);
    }

    private void showSosPopUpWindow(SosEvent event) {
        if (isFinishing() || isDestroyed()) return;

        if (activeSosDialog != null && activeSosDialog.isShowing()) {
            activeSosDialog.dismiss();
        }

        activeSosDialog = new Dialog(this);
        activeSosDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.dialog_sos_alert, null, false);
        activeSosDialog.setContentView(view);

        TextView dialogUserName = view.findViewById(R.id.dialogUserName);
        TextView dialogUserIdDept = view.findViewById(R.id.dialogUserIdDept);
        TextView dialogUserPhone = view.findViewById(R.id.dialogUserPhone);
        TextView dialogLocationLandmark = view.findViewById(R.id.dialogLocationLandmark);
        TextView dialogCoordinates = view.findViewById(R.id.dialogCoordinates);
        TextView dialogMedicalInfo = view.findViewById(R.id.dialogMedicalInfo);
        TextView dialogEmergencyContact = view.findViewById(R.id.dialogEmergencyContact);

        View btnDismissDialog = view.findViewById(R.id.btnDismissDialog);
        MaterialButton btnDispatchSecurity = view.findViewById(R.id.btnDispatchSecurity);
        MaterialButton btnCallStudent = view.findViewById(R.id.btnCallStudent);

        dialogUserName.setText(event.getUserName());
        dialogUserIdDept.setText(String.format(Locale.getDefault(), "ID: %s · %s", event.getUserId(), event.getUserDept()));
        dialogUserPhone.setText(String.format(Locale.getDefault(), "Phone: %s", event.getUserPhone()));
        dialogLocationLandmark.setText(event.getLocationLandmark());
        dialogCoordinates.setText(String.format(Locale.getDefault(), "Lat: %.5f° N, Lng: %.5f° E", event.getLatitude(), event.getLongitude()));
        dialogMedicalInfo.setText(String.format(Locale.getDefault(), "Blood: %s · %s", event.getBloodGroup(), event.getMedicalConditions()));
        dialogEmergencyContact.setText(event.getEmergencyContact());

        MapView dialogMapView = view.findViewById(R.id.dialogMapView);
        if (dialogMapView != null) {
            dialogMapView.onCreate(null);
            dialogMapView.onResume();
            dialogMapView.getMapAsync(googleMap -> {
                LatLng userLocation = new LatLng(event.getLatitude(), event.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16.5f));
                googleMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .title("🚨 " + event.getUserName() + " (SOS Location)"));
            });
        }

        View.OnClickListener openDirectMapsListener = v -> openDirectGoogleMaps(
                event.getLatitude(), event.getLongitude(), event.getUserName() + " (" + event.getLocationLandmark() + ")"
        );

        View btnOpenFullMap = view.findViewById(R.id.btnOpenFullMap);
        View cardDialogMap = view.findViewById(R.id.cardDialogMap);
        View layoutDialogLocation = view.findViewById(R.id.layoutDialogLocation);
        View btnOpenGoogleMaps = view.findViewById(R.id.btnOpenGoogleMaps);

        if (btnOpenFullMap != null) btnOpenFullMap.setOnClickListener(openDirectMapsListener);
        if (cardDialogMap != null) cardDialogMap.setOnClickListener(openDirectMapsListener);
        if (layoutDialogLocation != null) layoutDialogLocation.setOnClickListener(openDirectMapsListener);
        if (btnOpenGoogleMaps != null) btnOpenGoogleMaps.setOnClickListener(openDirectMapsListener);

        btnDismissDialog.setOnClickListener(v -> activeSosDialog.dismiss());

        btnDispatchSecurity.setOnClickListener(v -> {
            event.setStatus("DISPATCHED");
            Toast.makeText(this, "🚨 Security patrol dispatched to " + event.getLocationLandmark(), Toast.LENGTH_LONG).show();
            activeSosDialog.dismiss();
            refreshData();
        });

        btnCallStudent.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + event.getUserPhone()));
            startActivity(intent);
        });

        if (activeSosDialog.getWindow() != null) {
            activeSosDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        activeSosDialog.show();
    }

    private void openDirectGoogleMaps(double lat, double lng, String labelName) {
        String encodedLabel = Uri.encode("🚨 SOS Location: " + labelName);
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + encodedLabel + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            startActivity(webIntent);
        }
    }

    private void publishCampusAlert() {
        String title = etAlertTitle.getText().toString().trim();
        String location = etAlertLocation.getText().toString().trim();
        String desc = etAlertDesc.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etAlertTitle.setError("Title is required");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            location = "DHSGSU Campus";
        }

        String newId = String.valueOf(System.currentTimeMillis());
        Alert newAlert = new Alert(
                newId,
                title,
                location,
                desc.isEmpty() ? title : desc,
                "Just now",
                Alert.SEVERITY_CRITICAL,
                "Critical",
                location,
                "Campus Authority"
        );

        AlertRepository.getAlerts().add(0, newAlert);

        etAlertTitle.setText("");
        etAlertLocation.setText("");
        etAlertDesc.setText("");

        Toast.makeText(this, "📢 Alert published to all student/campus devices!", Toast.LENGTH_LONG).show();
    }
}
