package com.ewucsesummer25.quickdoc;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AllAppointmentsActivity extends AppCompatActivity {

    private LinearLayout allAppointmentsContainer;
    private Button btnBack;
    private DatabaseReference appointmentsRef;
    private String doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_appointments);

        allAppointmentsContainer = findViewById(R.id.allAppointmentsContainer);
        btnBack = findViewById(R.id.btnBackAllAppointments);

        doctorId = getIntent().getStringExtra("DOCTOR_ID");
        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        if (doctorId != null && !doctorId.isEmpty()) {
            loadAllAppointments();
        } else {
            Toast.makeText(this, "Doctor ID not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadAllAppointments() {
        Query query = appointmentsRef.orderByChild("doctorId").equalTo(doctorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allAppointmentsContainer.removeAllViews();
                if (snapshot.exists()) {
                    for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                        Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                        if (appointment != null) {
                            addAppointmentView(appointment);
                        }
                    }
                } else {
                    TextView noAppointmentsMsg = new TextView(AllAppointmentsActivity.this);
                    noAppointmentsMsg.setText("You have no appointments scheduled.");
                    noAppointmentsMsg.setPadding(0, 20, 0, 0);
                    allAppointmentsContainer.addView(noAppointmentsMsg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllAppointmentsActivity.this, "Failed to load appointments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAppointmentView(Appointment appointment) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View appointmentView = inflater.inflate(R.layout.appointment_list_item, allAppointmentsContainer, false);

        TextView tvPatientName = appointmentView.findViewById(R.id.tvAppointmentDoctorName);
        TextView tvDateTime = appointmentView.findViewById(R.id.tvAppointmentDateTime);


        appointmentView.findViewById(R.id.tvAppointmentSpecialization).setVisibility(View.GONE);
        appointmentView.findViewById(R.id.tvAppointmentLocation).setVisibility(View.GONE);

        tvPatientName.setText("Patient: " + appointment.getPatientName());
        tvDateTime.setText(appointment.getAppointmentDate() + " [" + appointment.getAppointmentTime() + "]");

        allAppointmentsContainer.addView(appointmentView);
    }
}
