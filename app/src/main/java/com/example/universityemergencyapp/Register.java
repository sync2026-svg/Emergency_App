package com.example.universityemergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Register extends AppCompatActivity {

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageButton btnBack = findViewById(R.id.btnBack);
        EditText etName = findViewById(R.id.etName);
        EditText etId = findViewById(R.id.etId);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhone = findViewById(R.id.etPhone);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);
        CheckBox cbTerms = findViewById(R.id.cbTerms);
        TextView btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnBack.setOnClickListener(v -> finish());

        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String id = etId.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (name.isEmpty()) {
                etName.setError("Enter your full name");
                etName.requestFocus();
                return;
            }
            if (id.isEmpty()) {
                etId.setError("Enter your student / employee ID");
                etId.requestFocus();
                return;
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid university email");
                etEmail.requestFocus();
                return;
            }
            if (phone.isEmpty() || phone.length() != 10) {
                etPhone.setError("Enter a valid 10-digit mobile number");
                etPhone.requestFocus();
                return;
            }
            if (password.isEmpty() || password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }
            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }
            if (!cbTerms.isChecked()) {
                Toast.makeText(this, "Please agree to share your details for emergency response", Toast.LENGTH_SHORT).show();
                return;
            }

            // Frontend-only: no backend account is actually created.
            Toast.makeText(this, "Account created! Welcome, " + name + ".", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Home.class));
            finish();
        });

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }
}
