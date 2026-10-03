package com.example.universityemergencyapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universityemergencyapp.push.SOS;
import com.example.universityemergencyapp.push.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class Home extends AppCompatActivity {

    FirebaseAuth auth ;
    FirebaseDatabase database ;
    FirebaseUser currrentUser ;
    double lat, lang ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        currrentUser = FirebaseAuth.getInstance().getCurrentUser() ;

        // ===== Big SOS button =====
        TextView btnSOS = findViewById(R.id.btnSOS);
        btnSOS.setOnClickListener(v -> {
            pushTheSOS() ;

        });

        // ===== Quick access grid =====
        ImageButton btnPolice = findViewById(R.id.btn3);
        ImageButton btnAmbulance = findViewById(R.id.btn4);
        ImageButton btnFire = findViewById(R.id.btn5);
        ImageButton btnSecurity = findViewById(R.id.btn6);
        ImageButton btnMedical = findViewById(R.id.btn7);
        ImageButton btnReport = findViewById(R.id.btn8);
        ImageButton btnMap = findViewById(R.id.btn9);
        ImageButton btnWellness = findViewById(R.id.btn10);

        btnPolice.setOnClickListener(v -> dial("100"));
        btnAmbulance.setOnClickListener(v -> dial("108"));
        btnFire.setOnClickListener(v -> dial("101"));
        btnSecurity.setOnClickListener(v -> dial("100"));
        btnMedical.setOnClickListener(v -> dial("108"));
        btnReport.setOnClickListener(v -> startActivity(new Intent(this, ReportIncidentActivity.class)));
        btnMap.setOnClickListener(v -> startActivity(new Intent(this, CampusMapActivity.class)));
        btnWellness.setOnClickListener(v ->
                Toast.makeText(this, "Counseling & Wellness — coming soon", Toast.LENGTH_SHORT).show());

        // ===== Recent alerts =====
        TextView tvSeeAllAlerts = findViewById(R.id.tvSeeAllAlerts);
        tvSeeAllAlerts.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));

        RecyclerView rvAlerts = findViewById(R.id.rvAlerts);
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        rvAlerts.setNestedScrollingEnabled(false);

        List<Alert> allAlerts = AlertRepository.getAlerts();
        List<Alert> recent = allAlerts.subList(0, Math.min(3, allAlerts.size()));
        AlertAdapter adapter = new AlertAdapter(recent, alert -> {
            Intent intent = new Intent(this, AlertDetailActivity.class);
            intent.putExtra(AlertDetailActivity.EXTRA_ALERT_ID, alert.getId());
            startActivity(intent);
        });
        rvAlerts.setAdapter(adapter);

        // ===== Bottom navigation =====
        BottomNavHelper.setup(this, BottomNavHelper.Tab.HOME);
    }

    private void dial(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        startActivity(intent);
    }

    void pushTheSOS() {

        String UID = currrentUser.getUid() ;
        SosActiveActivity soAct = new SosActiveActivity();



        DatabaseReference myref = FirebaseDatabase.getInstance().getReference("Students")

                  .child("SOS_Alert")
                  .child(currrentUser.getUid());
        lang= new SosActiveActivity().userLng;
        lat =new SosActiveActivity().userLat;
        SOS sos = new SOS(UID, lang, lat, "not assing", "Underprocess", "null");


        myref.setValue(sos).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    SosRepository.getInstance().triggerSosFromUser(Home.this);
                    startActivity(new Intent(Home.this, SosActiveActivity.class));
                    Toast.makeText(Home.this, "SOS send", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    public void getLongi(double longi){
        longi = lang;
    }
    public void getlati(double lati){
        lati = lat;
    }
}
