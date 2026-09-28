package com.example.universityemergencyapp;



import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.universityemergencyapp.push.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    private boolean isPasswordVisible = false;

    EditText etName, etId, etEmail, etPhone, etPassword, etConfirmPassword ;
    ImageButton btnTogglePassword, btnBack ;
    CheckBox cbTerms ;
    TextView btnRegister, tvGoLogin ;

    String name, id, email, phone, password, cnf_pass ;
    RadioGroup rgGender;
    RadioButton rbSelectedGender;

    FirebaseAuth auth ;
    FirebaseDatabase database ;
    FirebaseUser currentUser ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

         auth = FirebaseAuth.getInstance() ;
         database = FirebaseDatabase.getInstance();
         currentUser = FirebaseAuth.getInstance().getCurrentUser() ;

         btnBack = findViewById(R.id.btnBack);
         etName = findViewById(R.id.etName);
         etId = findViewById(R.id.etId);
         etEmail = findViewById(R.id.etEmail);
        rgGender = findViewById(R.id.rgGender);
         etPhone = findViewById(R.id.etPhone);
         etPassword = findViewById(R.id.etPassword);
         etConfirmPassword = findViewById(R.id.etConfirmPassword);
         btnTogglePassword = findViewById(R.id.btnTogglePassword);
         cbTerms = findViewById(R.id.cbTerms);
         btnRegister = findViewById(R.id.btnRegister);
         tvGoLogin = findViewById(R.id.tvGoLogin);

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
            name = etName.getText().toString().trim();
            id = etId.getText().toString().trim();
            email = etEmail.getText().toString().trim();
            phone = etPhone.getText().toString().trim();
            password = etPassword.getText().toString();
            cnf_pass = etConfirmPassword.getText().toString();

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
            int selectedGenderId = rgGender.getCheckedRadioButtonId();
            if (selectedGenderId == -1) {
                Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
                return;
            }
            rbSelectedGender = findViewById(selectedGenderId);
            String gender = rbSelectedGender.getText().toString();
            if (password.isEmpty() || password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }
            if (!password.equals(cnf_pass)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }
            if (!cbTerms.isChecked()) {
                Toast.makeText(this, "Please agree to share your details for emergency response", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(Register.this, "User is created", Toast.LENGTH_SHORT).show();

                                // Get the NEWLY created user
                                FirebaseUser firebaseUser = task.getResult().getUser();

                                if (firebaseUser != null) {
                                    String UID = firebaseUser.getUid();   // ← Correct UID

                                    User newStudent = new User(name, id, email, phone, "Male");

                                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Students");

                                    myRef.child(UID).setValue(newStudent)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(Register.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(Register.this, Home.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(Register.this, "Failed to save data", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }

                            } else {
                                Toast.makeText(Register.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            }
                        }

            }) .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(Register.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();

                }
            }) ;



        });

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }


}
