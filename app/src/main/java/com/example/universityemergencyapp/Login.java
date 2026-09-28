package com.example.universityemergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    EditText etEmail, etPassword ;
    ImageButton btnTogglePassword ;
    TextView btnLogin, btnGuest, tvForgotPassword, tvGoRegister ;
    String email, password ;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance() ;

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGuest = findViewById(R.id.btnGuest);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        //  logic for Toggle -> Hide/show password
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
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();

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

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {

                        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, Home.class));
                        finish();
                    }
                    else {
                        Toast.makeText(Login.this, "Not successful", Toast.LENGTH_SHORT).show();
                    }
                }
            });


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
