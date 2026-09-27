package com.example.universityemergencyapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "user_prefs";
    public static final String KEY_USER_NAME = "key_user_name";
    public static final String KEY_USER_ID = "key_user_id";
    public static final String KEY_USER_EMAIL = "key_user_email";
    public static final String KEY_USER_PHONE = "key_user_phone";
    public static final String KEY_USER_DEPARTMENT = "key_user_department";
    public static final String KEY_USER_HOSTEL = "key_user_hostel";

    private EditText etFullName;
    private EditText etStudentId;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etDepartment;
    private EditText etHostel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etFullName = findViewById(R.id.etFullName);
        etStudentId = findViewById(R.id.etStudentId);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etDepartment = findViewById(R.id.etDepartment);
        etHostel = findViewById(R.id.etHostel);
        TextView btnSaveProfile = findViewById(R.id.btnSaveProfile);

        loadProfileData();

        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        etFullName.setText(prefs.getString(KEY_USER_NAME, "Unknown"));
        etStudentId.setText(prefs.getString(KEY_USER_ID, "Unknown"));
        etEmail.setText(prefs.getString(KEY_USER_EMAIL, "Unknown"));
        etPhone.setText(prefs.getString(KEY_USER_PHONE, "Unknown"));
        etDepartment.setText(prefs.getString(KEY_USER_DEPARTMENT, "Unknown"));
        etHostel.setText(prefs.getString(KEY_USER_HOSTEL, "Unknown"));


    }

    private void saveProfileData() {
        String name = etFullName.getText().toString().trim();
        String id = etStudentId.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dept = etDepartment.getText().toString().trim();
        String hostel = etHostel.getText().toString().trim();



        if (name.isEmpty()) {
            etFullName.setError("Name cannot be empty");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_ID, id)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_PHONE, phone)
                .putString(KEY_USER_DEPARTMENT, dept)
                .putString(KEY_USER_HOSTEL, hostel)
                .apply();

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
