package com.example.universityemergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);
        TextView btnLogin = findViewById(R.id.btnLogin);
        TextView btnGuest = findViewById(R.id.btnGuest);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        // Show / hide password
        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Log in — frontend only, no backend call. Any valid-looking input succeeds.
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Enter your email or student ID");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email address");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Enter your password");
                return;
            }

            Toast.makeText(this, "Logging in…", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Home.class));
            finish();
        });

        // Guest access — straight to Home with limited/emergency-only features
        btnGuest.setOnClickListener(v -> {
            startActivity(new Intent(this, Home.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Forgot password flow coming soon", Toast.LENGTH_SHORT).show()
        );

        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, Register.class))
        );
    }
}
