package com.example.universityemergencyapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AuthoritySecurityActivity extends AppCompatActivity {

    private CardView cardAddSecurityForm;
    private EditText etGuardName;
    private EditText etBadgeId;
    private EditText etGuardPhone;
    private EditText etGuardVehicle;
    private EditText etGuardPost;

    private LinearLayout containerSecurityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_security);

        cardAddSecurityForm = findViewById(R.id.cardAddSecurityForm);
        MaterialButton btnToggleAddForm = findViewById(R.id.btnToggleAddForm);
        MaterialButton btnCancelAddForm = findViewById(R.id.btnCancelAddForm);
        MaterialButton btnAddSecurityPerson = findViewById(R.id.btnAddSecurityPerson);

        etGuardName = findViewById(R.id.etGuardName);
        etBadgeId = findViewById(R.id.etBadgeId);
        etGuardPhone = findViewById(R.id.etGuardPhone);
        etGuardVehicle = findViewById(R.id.etGuardVehicle);
        etGuardPost = findViewById(R.id.etGuardPost);

        containerSecurityList = findViewById(R.id.containerSecurityList);

        if (btnToggleAddForm != null) {
            btnToggleAddForm.setOnClickListener(v -> {
                boolean isVisible = cardAddSecurityForm.getVisibility() == View.VISIBLE;
                cardAddSecurityForm.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            });
        }

        if (btnCancelAddForm != null) {
            btnCancelAddForm.setOnClickListener(v -> cardAddSecurityForm.setVisibility(View.GONE));
        }

        btnAddSecurityPerson.setOnClickListener(v -> addSecurityOfficer());

        renderSecurityList();

        AuthorityNavHelper.setup(this, AuthorityNavHelper.Tab.SECURITY);
    }

    private void addSecurityOfficer() {
        String name = etGuardName.getText().toString().trim();
        String badge = etBadgeId.getText().toString().trim();
        String phone = etGuardPhone.getText().toString().trim();
        String vehicle = etGuardVehicle.getText().toString().trim();
        String post = etGuardPost.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etGuardName.setError("Officer name is required");
            return;
        }

        String id = "SEC_CUSTOM_" + System.currentTimeMillis();
        SecurityPerson person = new SecurityPerson(
                id,
                name,
                badge.isEmpty() ? "CP-Custom" : badge,
                phone.isEmpty() ? "+91 98765 00000" : phone,
                vehicle.isEmpty() ? "Patrol Bike" : vehicle,
                post.isEmpty() ? "Campus Gate" : post,
                "Available"
        );

        SecurityRepository.getInstance(this).addSecurityPerson(this, person);

        etGuardName.setText("");
        etBadgeId.setText("");
        etGuardPhone.setText("");
        etGuardVehicle.setText("");
        etGuardPost.setText("");

        if (cardAddSecurityForm != null) {
            cardAddSecurityForm.setVisibility(View.GONE);
        }

        renderSecurityList();
        Toast.makeText(this, "Officer " + name + " registered successfully!", Toast.LENGTH_SHORT).show();
    }

    private void renderSecurityList() {
        if (containerSecurityList == null) return;
        containerSecurityList.removeAllViews();

        List<SecurityPerson> list = SecurityRepository.getInstance(this).getSecurityPersons();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (SecurityPerson person : list) {
            View card = inflater.inflate(R.layout.item_security_person, containerSecurityList, false);

            ImageView ivProfileIcon = card.findViewById(R.id.ivProfileIcon);
            TextView tvGuardName = card.findViewById(R.id.tvGuardName);
            TextView tvGuardVehicle = card.findViewById(R.id.tvGuardVehicle);
            TextView tvGuardPhone = card.findViewById(R.id.tvGuardPhone);
            TextView tvGuardStatus = card.findViewById(R.id.tvGuardStatus);

            ImageButton btnDeleteSecurity = card.findViewById(R.id.btnDeleteSecurity);
            View layoutMainRow = card.findViewById(R.id.layoutMainRow);
            View layoutCallBar = card.findViewById(R.id.layoutCallBar);
            MaterialButton btnCallOfficer = card.findViewById(R.id.btnCallOfficer);

            if (ivProfileIcon != null) {
                ivProfileIcon.setImageResource(R.drawable.ic_person);
            }

            tvGuardName.setText(person.getName() + " (" + person.getBadgeId() + ")");
            tvGuardVehicle.setText(person.getVehicle() + " · " + person.getPostLocation());
            tvGuardPhone.setText("Phone: " + person.getPhone());
            tvGuardStatus.setText("Status: " + person.getStatus());

            if (btnCallOfficer != null) {
                btnCallOfficer.setText("Call " + person.getName());
                btnCallOfficer.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + person.getPhone().replace(" ", "")));
                    startActivity(intent);
                });
            }

            if (layoutMainRow != null && layoutCallBar != null) {
                layoutMainRow.setOnClickListener(v -> {
                    boolean isCurrentlyVisible = layoutCallBar.getVisibility() == View.VISIBLE;
                    layoutCallBar.setVisibility(isCurrentlyVisible ? View.GONE : View.VISIBLE);
                });
            }

            if (btnDeleteSecurity != null) {
                btnDeleteSecurity.setOnClickListener(v -> showDeleteOfficerDialog(person));
            }

            containerSecurityList.addView(card);
        }
    }

    private void showDeleteOfficerDialog(SecurityPerson person) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Security Officer")
                .setMessage("Are you sure you want to remove " + person.getName() + " from registered security personnel?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    SecurityRepository.getInstance(this).deleteSecurityPerson(this, person);
                    renderSecurityList();
                    Toast.makeText(this, "Officer " + person.getName() + " removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
