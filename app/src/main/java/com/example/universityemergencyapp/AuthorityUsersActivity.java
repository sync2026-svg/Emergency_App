package com.example.universityemergencyapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AuthorityUsersActivity extends AppCompatActivity {

    private TextView tvUser1Name;
    private TextView tvUser1Details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_users);

        tvUser1Name = findViewById(R.id.tvUser1Name);
        tvUser1Details = findViewById(R.id.tvUser1Details);

        loadUserData();

        AuthorityNavHelper.setup(this, AuthorityNavHelper.Tab.USERS);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(EditProfileActivity.PREFS_NAME, MODE_PRIVATE);

        String name = prefs.getString(EditProfileActivity.KEY_USER_NAME, "Aditi Sharma");
        String id = prefs.getString(EditProfileActivity.KEY_USER_ID, "DHSGSU2026041");
        String phone = prefs.getString(EditProfileActivity.KEY_USER_PHONE, "+91 98765 43210");
        String dept = prefs.getString(EditProfileActivity.KEY_USER_DEPARTMENT, "Computer Science & Applications");
        String blood = prefs.getString(MedicalInfoActivity.KEY_BLOOD_GROUP, "O+");
        String medical = prefs.getString(MedicalInfoActivity.KEY_CONDITIONS, "Asthma (Mild)");

        String pName = prefs.getString(EmergencyContactsActivity.KEY_PRIMARY_NAME, "Dr. Rajesh Sharma");
        String pPhone = prefs.getString(EmergencyContactsActivity.KEY_PRIMARY_PHONE, "+91 98765 12345");

        if (tvUser1Name != null) {
            tvUser1Name.setText(name);
        }
        if (tvUser1Details != null) {
            String details = "ID: " + id + " · " + dept + "\n" +
                    "Phone: " + phone + "\n" +
                    "Blood Group: " + blood + " · Medical: " + medical + "\n" +
                    "Emergency Contact: " + pName + " (" + pPhone + ")";
            tvUser1Details.setText(details);
        }
    }
}
