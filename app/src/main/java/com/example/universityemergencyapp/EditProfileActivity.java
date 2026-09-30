package com.example.universityemergencyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.universityemergencyapp.push.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    String name, id, email, phone, dept, hostel, gender ;
    FirebaseUser currentUser ;
    DatabaseReference myref ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

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
        if(currentUser != null) {
            myref = FirebaseDatabase.getInstance()
                    .getReference("Students")
                    .child(currentUser.getUid())
                    .child("Profile");

            //get data for database
            myref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        User student = snapshot.getValue(User.class);

                        etFullName.setText(student.getName());
                        etStudentId.setText(student.getId());
                        etEmail.setText(student.getEmail());
                        etPhone.setText(student.getPhone());
                        etDepartment.setText(student.getDepartment());
                        etHostel.setText(student.getHostel());
                        gender = student.getGender() ;

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });







        }

        btnSaveProfile.setOnClickListener(v -> {

            if(currentUser != null) {
                name = etFullName.getText().toString().trim();
                id = etStudentId.getText().toString().trim();
                email = etEmail.getText().toString().trim();
                phone = etPhone.getText().toString().trim();
                dept = etDepartment.getText().toString().trim();
                hostel = etHostel.getText().toString().trim();
                User newStudent = new User(name, id, email, phone, gender,dept,  hostel);
                myref = FirebaseDatabase.getInstance()
                        .getReference("Students")
                        .child(currentUser.getUid())
                        .child("Profile");
                myref.setValue(newStudent).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                        }
                    }
                });
            }

        });
//edit data in database


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


}
