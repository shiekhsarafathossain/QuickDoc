package com.ewucsesummer25.quickdoc;

import android.content.Intent;
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

public class doctor_lookup extends AppCompatActivity {

    private TextView tvDrName, tvShortBio, tvSpecialization, tvExperience, tvQualifications;
    private Button btnBookNow, btnBack;
    private ImageView ivProfileImage; // To display the doctor's picture

    private DatabaseReference doctorRef;
    private String doctorId, patientId, patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_lookup);

        // Get data from MainActivity
        doctorId = getIntent().getStringExtra("DOCTOR_ID");
        patientId = getIntent().getStringExtra("PATIENT_ID");
        patientName = getIntent().getStringExtra("PATIENT_NAME");

        // Initialize Views
        tvDrName = findViewById(R.id.tvDrName);
        tvShortBio = findViewById(R.id.tvShortBio);
        tvSpecialization = findViewById(R.id.tvSpecializationText);
        tvExperience = findViewById(R.id.tvExperienceText);
        tvQualifications = findViewById(R.id.tvQualificationsText);
        btnBookNow = findViewById(R.id.btnEditProfile); // This is the "Book Now" button
        btnBack = findViewById(R.id.btnBack);
        ivProfileImage = findViewById(R.id.imgUser); // Initialize the ImageView

        if (doctorId != null && !doctorId.isEmpty()) {
            doctorRef = FirebaseDatabase.getInstance().getReference("doctors").child(doctorId);
            loadDoctorDetails();
        } else {
            Toast.makeText(this, "Error: Doctor ID not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (patientId != null && patientName != null) {
                    Intent intent = new Intent(doctor_lookup.this, book_appointment.class);
                    intent.putExtra("PATIENT_ID", patientId);
                    intent.putExtra("PATIENT_NAME", patientName);
                    startActivity(intent);
                } else {
                    Toast.makeText(doctor_lookup.this, "Patient details are missing.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadDoctorDetails() {
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    if(doctor != null){
                        tvDrName.setText(doctor.getName());
                        tvShortBio.setText(doctor.getBio());
                        tvSpecialization.setText(doctor.getSpecialization());
                        tvExperience.setText(doctor.getExperience());
                        tvQualifications.setText(doctor.getQualification());

                        // Decode and set the profile image
                        String imageBase64 = doctor.getProfileImageBase64();
                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ivProfileImage.setImageBitmap(decodedByte);
                            } catch (Exception e) {
                                // If decoding fails, set a default image
                                ivProfileImage.setImageResource(R.drawable.doctor_1);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(doctor_lookup.this, "Failed to load doctor details.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

