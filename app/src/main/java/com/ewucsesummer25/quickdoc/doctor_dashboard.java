package com.ewucsesummer25.quickdoc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class doctor_dashboard extends AppCompatActivity {

    // UI elements from the XML layout
    private TextView tvDrName, tvShortBio, tvSpecializationText, tvExperienceText, tvQualificationsText;
    private Button btnEditProfile, btnEmergencySchedules, btnSeeAppointment, btnBack, btnLogout;
    private ImageView ivProfileImage; // For the profile picture

    // Firebase Database reference
    private DatabaseReference databaseReference;
    private String doctorId; // To store the ID of the logged-in doctor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_dashboard);

        // Initialize UI elements
        initializeViews();

        // Get the doctor's unique ID passed from the Doctor_Login activity
        Intent intent = getIntent();
        doctorId = intent.getStringExtra("DOCTOR_ID");

        // Initialize Firebase database reference to the specific doctor's node
        if (doctorId != null && !doctorId.isEmpty()) {
            databaseReference = FirebaseDatabase.getInstance().getReference("doctors").child(doctorId);
            loadDoctorProfile();
        } else {
            Toast.makeText(this, "Error: Doctor ID not found.", Toast.LENGTH_LONG).show();
            finish();
        }

        setupButtonClickListeners();
    }

    private void initializeViews() {
        tvDrName = findViewById(R.id.tvDrName);
        tvShortBio = findViewById(R.id.tvShortBio);
        tvSpecializationText = findViewById(R.id.tvSpecializationText);
        tvExperienceText = findViewById(R.id.tvExperienceText);
        tvQualificationsText = findViewById(R.id.tvQualificationsText);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEmergencySchedules = findViewById(R.id.btnEmergencySchedules);
        btnSeeAppointment = findViewById(R.id.btnSeeAppointment);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnDone);
        ivProfileImage = findViewById(R.id.imgUser); // Initialize ImageView
    }

    private void setupButtonClickListeners() {

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfileIntent = new Intent(doctor_dashboard.this, edit_profile_doctor.class);
                editProfileIntent.putExtra("DOCTOR_ID", doctorId);
                startActivity(editProfileIntent);
            }
        });

        // Set OnClickListener for the Logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the saved user session
                SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                // Navigate back to the welcome screen
                Intent logoutIntent = new Intent(doctor_dashboard.this, welcome_page.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logoutIntent);
                finish();
                Toast.makeText(doctor_dashboard.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnEmergencySchedules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(doctor_dashboard.this, "Emergency Schedules Clicked!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(doctor_dashboard.this, EmergencyScheduleActivity.class);
                i.putExtra("DOCTOR_ID", doctorId);
                startActivity(i);
            }
        });

        btnSeeAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(doctor_dashboard.this, "See Appointments Clicked!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(doctor_dashboard.this, AllAppointmentsActivity.class);
                i.putExtra("DOCTOR_ID", doctorId);
                startActivity(i);
            }
        });
    }


    private void loadDoctorProfile() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        // Populate the TextViews with data
                        tvDrName.setText("Dr. " + doctor.getName());
                        tvShortBio.setText(doctor.getBio());
                        tvSpecializationText.setText(doctor.getSpecialization());
                        tvExperienceText.setText(doctor.getExperience());
                        tvQualificationsText.setText(doctor.getQualification());

                        // Decode and display the profile image
                        String imageBase64 = doctor.getProfileImageBase64();
                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ivProfileImage.setImageBitmap(decodedByte);
                            } catch (Exception e) {
                                // If decoding fails, you can set a default image
                                ivProfileImage.setImageResource(R.drawable.doctor_1);
                            }
                        }
                    }
                } else {
                    Toast.makeText(doctor_dashboard.this, "Doctor profile not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(doctor_dashboard.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

