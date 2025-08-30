package com.ewucsesummer25.quickdoc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

public class patient_dashboard extends AppCompatActivity {

    private ImageView ivPatientProfile; // Added for the profile image
    private TextView tvPatientName, tvPatientAddress;
    private Button btnEditProfile, btnBookAppointment, btnEmergencyCalls, btnBack, btnLogout;
    private LinearLayout appointmentsContainer;

    private DatabaseReference patientRef, appointmentsRef, doctorsRef;
    private String patientId;
    private String patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_dashboard);

        patientId = getIntent().getStringExtra("PATIENT_ID");

        initializeViews();
        setupButtonClickListeners();

        if (patientId != null && !patientId.isEmpty()) {
            patientRef = FirebaseDatabase.getInstance().getReference("patients").child(patientId);
            appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
            doctorsRef = FirebaseDatabase.getInstance().getReference("doctors");
            loadPatientData();
            loadAppointments();
        } else {
            Toast.makeText(this, "Patient ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        ivPatientProfile = findViewById(R.id.imgUser); // Initialize the ImageView
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientAddress = findViewById(R.id.tvPatientAddress);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        btnEmergencyCalls = findViewById(R.id.btnEmergencyCalls);
        appointmentsContainer = findViewById(R.id.appointmentsContainer);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnDone);
    }

    private void setupButtonClickListeners() {
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(patient_dashboard.this, edit_profile_patient.class);
                intent.putExtra("PATIENT_ID", patientId);
                startActivity(intent);
            }
        });

        btnBookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(patient_dashboard.this, book_appointment.class);
                intent.putExtra("PATIENT_ID", patientId);
                intent.putExtra("PATIENT_NAME", patientName);
                startActivity(intent);
            }
        });

        btnEmergencyCalls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:01627400607"));
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutPatient();
            }
        });
    }

    private void loadPatientData() {
        patientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Patient patient = snapshot.getValue(Patient.class);
                    if (patient != null) {
                        patientName = patient.getName();
                        tvPatientName.setText(patientName);
                        tvPatientAddress.setText(patient.getAddress());

                        // --- Logic to Decode and Display Profile Image ---
                        if (patient.getProfileImageBase64() != null && !patient.getProfileImageBase64().isEmpty()) {
                            byte[] decodedString = Base64.decode(patient.getProfileImageBase64(), Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ivPatientProfile.setImageBitmap(decodedByte);
                        }
                        // --- End of Image Logic ---
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(patient_dashboard.this, "Failed to load patient data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAppointments() {
        Query query = appointmentsRef.orderByChild("patientId").equalTo(patientId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentsContainer.removeAllViews();
                if (snapshot.exists()) {
                    for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                        Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                        if (appointment != null) {
                            addAppointmentView(appointment);
                        }
                    }
                } else {
                    TextView noAppointmentsView = new TextView(patient_dashboard.this);
                    noAppointmentsView.setText("You have no scheduled appointments.");
                    noAppointmentsView.setPadding(8, 8, 8, 8);
                    appointmentsContainer.addView(noAppointmentsView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(patient_dashboard.this, "Failed to load appointments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAppointmentView(final Appointment appointment) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View appointmentView = inflater.inflate(R.layout.appointment_list_item, appointmentsContainer, false);

        final TextView tvDoctorName = appointmentView.findViewById(R.id.tvAppointmentDoctorName);
        final TextView tvSpecialization = appointmentView.findViewById(R.id.tvAppointmentSpecialization);
        final TextView tvLocation = appointmentView.findViewById(R.id.tvAppointmentLocation);
        final TextView tvDateTime = appointmentView.findViewById(R.id.tvAppointmentDateTime);

        tvDoctorName.setText(appointment.getDoctorName());
        tvDateTime.setText(appointment.getAppointmentDate() + " [" + appointment.getAppointmentTime() + "]");

        DatabaseReference doctorRef = doctorsRef.child(appointment.getDoctorId());
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        tvSpecialization.setText(doctor.getSpecialization());
                        tvLocation.setText(doctor.getAddress());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Fields will remain blank if doctor details fail to load
            }
        });

        appointmentsContainer.addView(appointmentView);
    }

    private void logoutPatient() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(patient_dashboard.this, welcome_page.class);
        startActivity(intent);
        finishAffinity();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}

