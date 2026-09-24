package com.example.universityemergencyapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AuthorityHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_history);

        AuthorityNavHelper.setup(this, AuthorityNavHelper.Tab.HISTORY);
    }
}
