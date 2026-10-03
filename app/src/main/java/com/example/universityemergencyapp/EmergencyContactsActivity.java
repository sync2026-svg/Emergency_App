package com.example.universityemergencyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.universityemergencyapp.push.Econtact;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EmergencyContactsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "user_prefs";
    public static final String KEY_CONTACT_COUNT = "contact_count";

    public static final String KEY_PRIMARY_NAME = "primary_contact_name";
    public static final String KEY_PRIMARY_RELATION = "primary_contact_relation";
    public static final String KEY_PRIMARY_PHONE = "primary_contact_phone";

    public static final String KEY_SECONDARY_NAME = "secondary_contact_name";
    public static final String KEY_SECONDARY_RELATION = "secondary_contact_relation";
    public static final String KEY_SECONDARY_PHONE = "secondary_contact_phone";

    public static final String KEY_C3_NAME = "c3_contact_name";
    public static final String KEY_C3_RELATION = "c3_contact_relation";
    public static final String KEY_C3_PHONE = "c3_contact_phone";

    public static final String KEY_C4_NAME = "c4_contact_name";
    public static final String KEY_C4_RELATION = "c4_contact_relation";
    public static final String KEY_C4_PHONE = "c4_contact_phone";

    public static final String KEY_C5_NAME = "c5_contact_name";
    public static final String KEY_C5_RELATION = "c5_contact_relation";
    public static final String KEY_C5_PHONE = "c5_contact_phone";
    private EditText etPrimaryName, etPrimaryRelation, etPrimaryPhone;
    private EditText etSecondaryName, etSecondaryRelation, etSecondaryPhone;
    private CardView cardContact3, cardContact4, cardContact5;
    private EditText etContact3Name, etContact3Relation, etContact3Phone;
    private EditText etContact4Name, etContact4Relation, etContact4Phone;
    private EditText etContact5Name, etContact5Relation, etContact5Phone;

    private TextView btnAddMoreContact;
    private int visibleCount = 2;
    private FirebaseAuth auth ;
    private FirebaseDatabase database ;
    private FirebaseUser currentuser ;
    String name , relation , phone, secName, secrelation, secphone ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        auth = FirebaseAuth.getInstance() ;
        database = FirebaseDatabase.getInstance() ;
        currentuser = FirebaseAuth.getInstance().getCurrentUser() ;

        etPrimaryName = findViewById(R.id.etPrimaryName);
        etPrimaryRelation = findViewById(R.id.etPrimaryRelation);

        etPrimaryPhone = findViewById(R.id.etPrimaryPhone);
        ImageButton btnCallPrimary = findViewById(R.id.btnCallPrimary);

        etSecondaryName = findViewById(R.id.etSecondaryName);
        etSecondaryRelation = findViewById(R.id.etSecondaryRelation);
        etSecondaryPhone = findViewById(R.id.etSecondaryPhone);
        ImageButton btnCallSecondary = findViewById(R.id.btnCallSecondary);

        cardContact3 = findViewById(R.id.cardContact3);
        etContact3Name = findViewById(R.id.etContact3Name);
        etContact3Relation = findViewById(R.id.etContact3Relation);
        etContact3Phone = findViewById(R.id.etContact3Phone);
        ImageButton btnRemoveContact3 = findViewById(R.id.btnRemoveContact3);

        cardContact4 = findViewById(R.id.cardContact4);
        etContact4Name = findViewById(R.id.etContact4Name);
        etContact4Relation = findViewById(R.id.etContact4Relation);
        etContact4Phone = findViewById(R.id.etContact4Phone);
        ImageButton btnRemoveContact4 = findViewById(R.id.btnRemoveContact4);

        cardContact5 = findViewById(R.id.cardContact5);
        etContact5Name = findViewById(R.id.etContact5Name);
        etContact5Relation = findViewById(R.id.etContact5Relation);
        etContact5Phone = findViewById(R.id.etContact5Phone);
        ImageButton btnRemoveContact5 = findViewById(R.id.btnRemoveContact5);

        btnAddMoreContact = findViewById(R.id.btnAddMoreContact);
        TextView btnSaveContacts = findViewById(R.id.btnSaveContacts);

        loadContactsData();

        if(currentuser != null) {

            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Students")
                    .child(currentuser.getUid())
                    .child("Emergency_contact");

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        Econtact econtactq = snapshot.getValue(Econtact.class) ;
                        etPrimaryName.setText(econtactq.getName());
                        etPrimaryRelation.setText(econtactq.getRelation()) ;
                        etPrimaryPhone.setText(econtactq.getPhone()) ;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        btnCallPrimary.setOnClickListener(v -> dial(etPrimaryPhone.getText().toString().trim()));
        btnCallSecondary.setOnClickListener(v -> dial(etSecondaryPhone.getText().toString().trim()));

        btnAddMoreContact.setOnClickListener(v -> addNextContact());

        btnRemoveContact3.setOnClickListener(v -> removeContact(3));
        btnRemoveContact4.setOnClickListener(v -> removeContact(4));
        btnRemoveContact5.setOnClickListener(v -> removeContact(5));
        btnSaveContacts.setOnClickListener(v -> saveContactsData());
        btnSaveContacts.setOnClickListener(v -> {

            Toast.makeText(EmergencyContactsActivity.this, "Lungi madhrchod", Toast.LENGTH_SHORT).show();
            //Saving data

            if(currentuser != null) {

                name = etPrimaryName.getText().toString().trim() ;
                relation = etPrimaryRelation.getText().toString().trim() ;
                phone = etPrimaryPhone.getText().toString().trim() ;
                secName = etSecondaryName.getText().toString().trim() ;
                secrelation = etSecondaryRelation.getText().toString().trim() ;
                secphone = etSecondaryPhone.getText().toString().trim() ;

                if(secName.isEmpty()) {secName = "null"; }
                if(secrelation.isEmpty()) {secrelation = "null"; }
                if(secphone.isEmpty()){secphone = ""; }

                Econtact emc = new Econtact() ;
                emc.setName(name);
                emc.setRelation(relation);
                emc.setPhone(phone);

                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Students")
                        .child(currentuser.getUid())
                        .child("Emergency_contact");
                myRef.setValue(emc).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){


                        }
                        else {
                            Toast.makeText(EmergencyContactsActivity.this, "Try again", Toast.LENGTH_SHORT).show() ;
                        }
                    }
                });
            }
        });

    }

private void addNextContact() {
    if (visibleCount < 5) {
        visibleCount++;
        updateCardVisibilities();
    } else {
        Toast.makeText(this, "Maximum 5 emergency contacts allowed", Toast.LENGTH_SHORT).show();
    }
}


    private void removeContact(int index) {
        if (index == 3) {
            if (visibleCount >= 5) {
                copyFields(etContact4Name, etContact3Name);
                copyFields(etContact4Relation, etContact3Relation);
                copyFields(etContact4Phone, etContact3Phone);

                copyFields(etContact5Name, etContact4Name);
                copyFields(etContact5Relation, etContact4Relation);
                copyFields(etContact5Phone, etContact4Phone);

                clearFields(etContact5Name, etContact5Relation, etContact5Phone);
            } else if (visibleCount == 4) {
                copyFields(etContact4Name, etContact3Name);
                copyFields(etContact4Relation, etContact3Relation);
                copyFields(etContact4Phone, etContact3Phone);

                clearFields(etContact4Name, etContact4Relation, etContact4Phone);
            } else {
                clearFields(etContact3Name, etContact3Relation, etContact3Phone);
            }
        } else if (index == 4) {
            if (visibleCount >= 5) {
                copyFields(etContact5Name, etContact4Name);
                copyFields(etContact5Relation, etContact4Relation);
                copyFields(etContact5Phone, etContact4Phone);

                clearFields(etContact5Name, etContact5Relation, etContact5Phone);
            } else {
                clearFields(etContact4Name, etContact4Relation, etContact4Phone);
            }
        } else if (index == 5) {
            clearFields(etContact5Name, etContact5Relation, etContact5Phone);
        }

        if (visibleCount > 2) {
            visibleCount--;
        }
        updateCardVisibilities();
    }

    private void copyFields(EditText src, EditText dest) {
        dest.setText(src.getText().toString());
    }

    private void clearFields(EditText... fields) {
        for (EditText field : fields) {
            field.setText("");
        }
    }

    private void updateCardVisibilities() {
        cardContact3.setVisibility(visibleCount >= 3 ? View.VISIBLE : View.GONE);
        cardContact4.setVisibility(visibleCount >= 4 ? View.VISIBLE : View.GONE);
        cardContact5.setVisibility(visibleCount >= 5 ? View.VISIBLE : View.GONE);

        btnAddMoreContact.setVisibility(visibleCount < 5 ? View.VISIBLE : View.GONE);
    }

    private void loadContactsData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        etPrimaryName.setText(prefs.getString(KEY_PRIMARY_NAME, "Null"));
        etPrimaryRelation.setText(prefs.getString(KEY_PRIMARY_RELATION, "Null"));
        etPrimaryPhone.setText(prefs.getString(KEY_PRIMARY_PHONE, "Null"));

        etSecondaryName.setText(prefs.getString(KEY_SECONDARY_NAME, "Null"));
        etSecondaryRelation.setText(prefs.getString(KEY_SECONDARY_RELATION, "Null"));
        etSecondaryPhone.setText(prefs.getString(KEY_SECONDARY_PHONE, "Null"));

        String c3Name = prefs.getString(KEY_C3_NAME, "");
        String c3Rel = prefs.getString(KEY_C3_RELATION, "");
        String c3Phone = prefs.getString(KEY_C3_PHONE, "");

        String c4Name = prefs.getString(KEY_C4_NAME, "");
        String c4Rel = prefs.getString(KEY_C4_RELATION, "");
        String c4Phone = prefs.getString(KEY_C4_PHONE, "");

        String c5Name = prefs.getString(KEY_C5_NAME, "");
        String c5Rel = prefs.getString(KEY_C5_RELATION, "");
        String c5Phone = prefs.getString(KEY_C5_PHONE, "");

        etContact3Name.setText(c3Name);
        etContact3Relation.setText(c3Rel);
        etContact3Phone.setText(c3Phone);

        etContact4Name.setText(c4Name);
        etContact4Relation.setText(c4Rel);
        etContact4Phone.setText(c4Phone);

        etContact5Name.setText(c5Name);
        etContact5Relation.setText(c5Rel);
        etContact5Phone.setText(c5Phone);

        int savedCount = prefs.getInt(KEY_CONTACT_COUNT, 2);
        if (!c5Phone.isEmpty() || !c5Name.isEmpty()) {
            savedCount = Math.max(savedCount, 5);
        } else if (!c4Phone.isEmpty() || !c4Name.isEmpty()) {
            savedCount = Math.max(savedCount, 4);
        } else if (!c3Phone.isEmpty() || !c3Name.isEmpty()) {
            savedCount = Math.max(savedCount, 3);
        }

        visibleCount = Math.min(5, Math.max(2, savedCount));
        updateCardVisibilities();
    }

    private void saveContactsData() {
        String pPhone = etPrimaryPhone.getText().toString().trim();
        if (pPhone.isEmpty()) {
            etPrimaryPhone.setError("Primary contact phone is required");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_CONTACT_COUNT, visibleCount)
                .putString(KEY_PRIMARY_NAME, etPrimaryName.getText().toString().trim())
                .putString(KEY_PRIMARY_RELATION, etPrimaryRelation.getText().toString().trim())
                .putString(KEY_PRIMARY_PHONE, pPhone)

                .putString(KEY_SECONDARY_NAME, etSecondaryName.getText().toString().trim())
                .putString(KEY_SECONDARY_RELATION, etSecondaryRelation.getText().toString().trim())
                .putString(KEY_SECONDARY_PHONE, etSecondaryPhone.getText().toString().trim())

                .putString(KEY_C3_NAME, visibleCount >= 3 ? etContact3Name.getText().toString().trim() : "")
                .putString(KEY_C3_RELATION, visibleCount >= 3 ? etContact3Relation.getText().toString().trim() : "")
                .putString(KEY_C3_PHONE, visibleCount >= 3 ? etContact3Phone.getText().toString().trim() : "")

                .putString(KEY_C4_NAME, visibleCount >= 4 ? etContact4Name.getText().toString().trim() : "")
                .putString(KEY_C4_RELATION, visibleCount >= 4 ? etContact4Relation.getText().toString().trim() : "")
                .putString(KEY_C4_PHONE, visibleCount >= 4 ? etContact4Phone.getText().toString().trim() : "")

                .putString(KEY_C5_NAME, visibleCount >= 5 ? etContact5Name.getText().toString().trim() : "")
                .putString(KEY_C5_RELATION, visibleCount >= 5 ? etContact5Relation.getText().toString().trim() : "")
                .putString(KEY_C5_PHONE, visibleCount >= 5 ? etContact5Phone.getText().toString().trim() : "")
                .apply();

        Toast.makeText(this, "Emergency contacts saved (" + visibleCount + " contacts)", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void dial(String phoneNumber) {
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "No phone number provided", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }
}