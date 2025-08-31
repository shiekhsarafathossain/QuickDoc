package com.ewucsesummer25.quickdoc;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageView ivPatientProfile;
    private TextView tvUserName, tvAddress;
    private Button btnProfileInfo, btnLogoutPatient;
    private RelativeLayout upcomingConsultationLayout;
    private TextView tvDoctorName, tvDoctorConsultationTime, tvUpcomingConsultantTitle, tvDoctorAddressConsult, tvDoctorSpecializationConsult;
    private LinearLayout doctorsContainer;
    private DatabaseReference patientRef, appointmentsRef, doctorsRef;
    private String patientId;
    private String patientName;

    private BroadcastReceiver appointmentReminderReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ivPatientProfile = findViewById(R.id.userIcon);
        tvUserName = findViewById(R.id.tvUserName);
        tvAddress = findViewById(R.id.tvAddress);
        btnProfileInfo = findViewById(R.id.btnProfileInfo);
        upcomingConsultationLayout = findViewById(R.id.rl2);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDoctorConsultationTime = findViewById(R.id.tvDoctorConsultationTime);
        tvUpcomingConsultantTitle = findViewById(R.id.tvUpcomingConsultant);
        doctorsContainer = findViewById(R.id.doctorsContainer);
        btnLogoutPatient = findViewById(R.id.btnLogoutPatient);
        tvDoctorAddressConsult = findViewById(R.id.tvDoctorAddressConsult);
        tvDoctorSpecializationConsult = findViewById(R.id.tvDoctorSpecializationConsult);

        upcomingConsultationLayout.setVisibility(View.GONE);
        tvUpcomingConsultantTitle.setVisibility(View.GONE);

        btnProfileInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (patientId != null) {
                    Intent intent = new Intent(MainActivity.this, patient_dashboard.class);
                    intent.putExtra("PATIENT_ID", patientId);
                    startActivity(intent);
                }
            }
        });

        btnLogoutPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutPatient();
            }
        });

        patientId = getIntent().getStringExtra("PATIENT_ID");

        if (patientId != null && !patientId.isEmpty()) {
            patientRef = FirebaseDatabase.getInstance().getReference("patients").child(patientId);
            appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
            doctorsRef = FirebaseDatabase.getInstance().getReference("doctors");

            loadPatientData();
            loadUpcomingAppointment();
            loadAllDoctors();
        } else {
            Toast.makeText(this, "Error: Patient ID not found.", Toast.LENGTH_LONG).show();
            finish();
        }

        setupAppointmentReceiver();
    }

    private void setupAppointmentReceiver() {
        appointmentReminderReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String doctorName = intent.getStringExtra("DOCTOR_NAME");
                String appointmentTime = intent.getStringExtra("APPOINTMENT_TIME");
                int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);

                showAppointmentDialog(doctorName, appointmentTime);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationId);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                appointmentReminderReceiver,
                new IntentFilter(AppointmentNotificationReceiver.ACTION_SHOW_APPOINTMENT_REMINDER)
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(appointmentReminderReceiver);
    }

    private void showAppointmentDialog(String doctorName, String appointmentTime) {
        new AlertDialog.Builder(this)
                .setTitle("Appointment Reminder")
                .setMessage("You have an appointment with " + doctorName + " at " + appointmentTime + ".")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void loadPatientData() {
        patientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Patient patient = snapshot.getValue(Patient.class);
                    if (patient != null) {
                        patientName = patient.getName();
                        tvUserName.setText(patientName);
                        tvAddress.setText(patient.getAddress());

                        if (patient.getProfileImageBase64() != null && !patient.getProfileImageBase64().isEmpty()) {
                            byte[] decodedString = Base64.decode(patient.getProfileImageBase64(), Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ivPatientProfile.setImageBitmap(decodedByte);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load patient data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUpcomingAppointment() {
        Query query = appointmentsRef.orderByChild("patientId").equalTo(patientId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Appointment upcomingAppointment = null;
                String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                    if (appointment != null && appointment.getAppointmentDate().compareTo(todayDate) >= 0) {
                        if (upcomingAppointment == null || appointment.getAppointmentDate().compareTo(upcomingAppointment.getAppointmentDate()) < 0) {
                            upcomingAppointment = appointment;
                        }
                    }
                }

                if (upcomingAppointment != null) {
                    displayUpcomingAppointment(upcomingAppointment);
                } else {
                    upcomingConsultationLayout.setVisibility(View.GONE);
                    tvUpcomingConsultantTitle.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load appointments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUpcomingAppointment(Appointment appointment) {
        DatabaseReference doctorForAppointmentRef = doctorsRef.child(appointment.getDoctorId());
        doctorForAppointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        tvDoctorName.setText(appointment.getDoctorName());
                        tvDoctorSpecializationConsult.setText(doctor.getSpecialization());
                        tvDoctorAddressConsult.setText(doctor.getAddress());
                        tvDoctorConsultationTime.setText(appointment.getAppointmentDate() + " [" + appointment.getAppointmentTime() + "]");
                        upcomingConsultationLayout.setVisibility(View.VISIBLE);
                        tvUpcomingConsultantTitle.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvDoctorName.setText(appointment.getDoctorName());
                tvDoctorConsultationTime.setText(appointment.getAppointmentDate() + " [" + appointment.getAppointmentTime() + "]");
                upcomingConsultationLayout.setVisibility(View.VISIBLE);
                tvUpcomingConsultantTitle.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadAllDoctors() {
        doctorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorsContainer.removeAllViews();
                for (DataSnapshot doctorSnapshot : snapshot.getChildren()) {
                    Doctor doctor = doctorSnapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        doctor.setDoctorId(doctorSnapshot.getKey());
                        addDoctorViewToContainer(doctor);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load doctors list.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addDoctorViewToContainer(Doctor doctor) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View doctorView = inflater.inflate(R.layout.doctor_list_item, doctorsContainer, false);

        ImageView ivDocProfile = doctorView.findViewById(R.id.doctorIconItem);
        TextView tvDocName = doctorView.findViewById(R.id.tvDoctorNameItem);
        TextView tvDocSpec = doctorView.findViewById(R.id.tvDoctorSpecItem);
        TextView tvDocAddress = doctorView.findViewById(R.id.tvDoctorAddressItem);
        Button btnProfile = doctorView.findViewById(R.id.btnDoctorProfileItem);

        tvDocName.setText(doctor.getName());
        tvDocSpec.setText(doctor.getSpecialization());
        tvDocAddress.setText(doctor.getAddress());

        String imageBase64 = doctor.getProfileImageBase64();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivDocProfile.setImageBitmap(decodedByte);
            } catch (Exception e) {
                ivDocProfile.setImageResource(R.drawable.doctor_1);
            }
        }

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, doctor_lookup.class);
                intent.putExtra("DOCTOR_ID", doctor.getDoctorId());
                intent.putExtra("PATIENT_ID", patientId);
                intent.putExtra("PATIENT_NAME", patientName);
                startActivity(intent);
            }
        });

        doctorsContainer.addView(doctorView);
    }

    private void logoutPatient() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(MainActivity.this, welcome_page.class);
        startActivity(intent);
        finishAffinity();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}