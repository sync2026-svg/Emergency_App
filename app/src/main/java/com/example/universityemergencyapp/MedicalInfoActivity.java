package com.example.universityemergencyapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MedicalInfoActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "user_prefs";
    public static final String KEY_BLOOD_GROUP = "medical_blood_group";
    public static final String KEY_CONDITIONS = "medical_conditions";
    public static final String KEY_ALLERGIES = "medical_allergies";
    public static final String KEY_MEDICATIONS = "medical_medications";
    public static final String KEY_INSURANCE = "medical_insurance";

    private static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};

    private Spinner spinnerBloodGroup;
    private EditText etConditions;
    private EditText etAllergies;
    private EditText etMedications;
    private EditText etInsurance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_info);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);
        findViewById(R.id.layoutBloodGroup).setOnClickListener(v -> spinnerBloodGroup.performClick());
        etConditions = findViewById(R.id.etConditions);
        etAllergies = findViewById(R.id.etAllergies);
        etMedications = findViewById(R.id.etMedications);
        etInsurance = findViewById(R.id.etInsurance);
        TextView btnSaveMedical = findViewById(R.id.btnSaveMedical);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                BLOOD_GROUPS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(adapter);

        loadMedicalData();

        btnSaveMedical.setOnClickListener(v -> saveMedicalData());
    }

    private void loadMedicalData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedBloodGroup = prefs.getString(KEY_BLOOD_GROUP, "Null");
        etConditions.setText(prefs.getString(KEY_CONDITIONS, "Asthma (Mild)"));
        etAllergies.setText(prefs.getString(KEY_ALLERGIES, "Penicillin, Dust"));
        etMedications.setText(prefs.getString(KEY_MEDICATIONS, "Inhaler (Salbutamol)"));
        etInsurance.setText(prefs.getString(KEY_INSURANCE, "DHSGSU-MED-88392"));

        // Select blood group in dropdown
        for (int i = 0; i < BLOOD_GROUPS.length; i++) {
            if (BLOOD_GROUPS[i].equalsIgnoreCase(savedBloodGroup)) {
                spinnerBloodGroup.setSelection(i);
                break;
            }
        }
    }

    private void saveMedicalData() {
        String selectedBloodGroup = "O+";
        if (spinnerBloodGroup.getSelectedItem() != null) {
            selectedBloodGroup = spinnerBloodGroup.getSelectedItem().toString();
        }

        String conditions = etConditions.getText().toString().trim();
        String allergies = etAllergies.getText().toString().trim();
        String medications = etMedications.getText().toString().trim();
        String insurance = etInsurance.getText().toString().trim();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_BLOOD_GROUP, selectedBloodGroup)
                .putString(KEY_CONDITIONS, conditions)
                .putString(KEY_ALLERGIES, allergies)
                .putString(KEY_MEDICATIONS, medications)
                .putString(KEY_INSURANCE, insurance)
                .apply();

        Toast.makeText(this, "Medical information updated", Toast.LENGTH_SHORT).show();
        finish();
    }
}
