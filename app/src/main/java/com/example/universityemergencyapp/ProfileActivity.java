package com.example.universityemergencyapp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.universityemergencyapp.push.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileActivity extends AppCompatActivity {

    DatabaseReference myRef ;
    FirebaseUser currentUser ;

    TextView userName, student_id ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userName = findViewById(R.id.tvUserName);
        student_id = findViewById(R.id.tvUserMeta) ;

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        LinearLayout rowEditProfile = findViewById(R.id.rowEditProfile);
        LinearLayout rowEmergencyContacts = findViewById(R.id.rowEmergencyContacts);
        LinearLayout rowMedicalInfo = findViewById(R.id.rowMedicalInfo);
        LinearLayout rowSafetyGuide = findViewById(R.id.rowSafetyGuide);
        LinearLayout rowHelpCenter = findViewById(R.id.rowHelpCenter);
        LinearLayout rowMyReports = findViewById(R.id.rowMyReports);
        Switch switchLocationSharing = findViewById(R.id.switchLocationSharing);
        Switch switchPushAlerts = findViewById(R.id.switchPushAlerts);
        TextView btnLogout = findViewById(R.id.btnLogout);

        if (currentUser != null) {
            String uid = currentUser.getUid();
            myRef = FirebaseDatabase.getInstance().getReference("Students").child(currentUser.getUid()).child("Profile");

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User student = snapshot.getValue(User.class);  // Use simple User (make sure import is correct)

                        if (student != null) {

                            userName.setText(student.getName());
                            student_id.setText("Student · ID "+student.getId());
                            String username = student.getName() ;
                            new Intent(ProfileActivity.this, Home.class).putExtra("USER_NAME", username) ;

                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "No data found for this user", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        View.OnClickListener comingSoon = v ->
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();

        rowEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });
        rowEmergencyContacts.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EmergencyContactsActivity.class));

        });
        rowMedicalInfo.setOnClickListener(v ->{
            startActivity(new Intent(ProfileActivity.this, MedicalInfoActivity.class));
            startActivity(new Intent(this, Register.class));
        });


        rowSafetyGuide.setOnClickListener(comingSoon);
        rowHelpCenter.setOnClickListener(comingSoon);
        rowMyReports.setOnClickListener(comingSoon);

        switchLocationSharing.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                Toast.makeText(this,
                        isChecked ? "Live location sharing during SOS enabled" : "Live location sharing during SOS disabled",
                        Toast.LENGTH_SHORT).show());

        switchPushAlerts.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                Toast.makeText(this,
                        isChecked ? "Push notifications enabled" : "Push notifications disabled",
                        Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "land boor is comming", Toast.LENGTH_SHORT).show();

        });

        BottomNavHelper.setup(this, BottomNavHelper.Tab.PROFILE);
    }
}