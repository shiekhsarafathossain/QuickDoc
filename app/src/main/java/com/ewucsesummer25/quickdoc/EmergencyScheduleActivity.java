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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmergencyScheduleActivity extends AppCompatActivity {

    private LinearLayout appointmentsContainer;
    private Button btnBack;
    private DatabaseReference appointmentsRef;
    private String doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_schedule);

        appointmentsContainer = findViewById(R.id.appointmentsListContainer);
        btnBack = findViewById(R.id.btnBack);

        doctorId = getIntent().getStringExtra("DOCTOR_ID");
        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        if (doctorId != null && !doctorId.isEmpty()) {
            loadTodaysAppointments();
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

    private void loadTodaysAppointments() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Query query = appointmentsRef.orderByChild("doctorId").equalTo(doctorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentsContainer.removeAllViews();
                boolean hasAppointmentsToday = false;

                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                    if (appointment != null && appointment.getAppointmentDate().equals(todayDate)) {
                        hasAppointmentsToday = true;
                        addAppointmentViewToList(appointment);
                    }
                }

                if (!hasAppointmentsToday) {
                    TextView noAppointmentsMsg = new TextView(EmergencyScheduleActivity.this);
                    noAppointmentsMsg.setText("No appointments scheduled for today.");
                    noAppointmentsMsg.setPadding(0, 20, 0, 0);
                    appointmentsContainer.addView(noAppointmentsMsg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmergencyScheduleActivity.this, "Failed to load appointments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAppointmentViewToList(Appointment appointment) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View appointmentView = inflater.inflate(R.layout.appointment_list_item, appointmentsContainer, false);

        TextView tvPatientName = appointmentView.findViewById(R.id.tvAppointmentDoctorName);
        TextView tvTime = appointmentView.findViewById(R.id.tvAppointmentDateTime);


        appointmentView.findViewById(R.id.tvAppointmentSpecialization).setVisibility(View.GONE);
        appointmentView.findViewById(R.id.tvAppointmentLocation).setVisibility(View.GONE);

        tvPatientName.setText("Patient: " + appointment.getPatientName());
        tvTime.setText("Time: " + appointment.getAppointmentTime());

        appointmentsContainer.addView(appointmentView);
    }
}
