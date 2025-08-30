package com.ewucsesummer25.quickdoc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class edit_profile_doctor extends AppCompatActivity {

    private EditText etName, etUsername, etEmail, etPhoneNumber, etAddress, etPostalCode,
            etBio, etSpecialization, etExperience, etQualification;
    private Button btnBack, btnDone;
    private ImageView ivProfileImage; // For the profile picture

    private DatabaseReference databaseReference;
    private String doctorId;
    private String imageBase64; // To hold new image data

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_doctor);

        initializeViews();
        initializeLaunchers();

        Intent intent = getIntent();
        doctorId = intent.getStringExtra("DOCTOR_ID");

        if (doctorId != null && !doctorId.isEmpty()) {
            databaseReference = FirebaseDatabase.getInstance().getReference("doctors").child(doctorId);
            loadDoctorInfo();
        } else {
            Toast.makeText(this, "Error: Could not find doctor profile.", Toast.LENGTH_LONG).show();
            finish();
        }

        setupButtonClickListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etEditName);
        etUsername = findViewById(R.id.etEditUsername);
        etEmail = findViewById(R.id.etEditEmail);
        etPhoneNumber = findViewById(R.id.etEditPhoneNumber);
        etAddress = findViewById(R.id.etEditAddress);
        etPostalCode = findViewById(R.id.etEditPostalCode);
        etBio = findViewById(R.id.etEditBio);
        etSpecialization = findViewById(R.id.etEditSpecialization);
        etExperience = findViewById(R.id.etEditExperience);
        etQualification = findViewById(R.id.etEditQualification);
        btnBack = findViewById(R.id.btnBack);
        btnDone = findViewById(R.id.btnDone);
        ivProfileImage = findViewById(R.id.imgUser); // Correct ID for the doctor's ImageView
    }

    private void setupButtonClickListeners() {
        btnDone.setOnClickListener(v -> updateDoctorProfile());
        btnBack.setOnClickListener(v -> finish());
        ivProfileImage.setOnClickListener(v -> checkPermissionAndOpenGallery());
    }

    private void loadDoctorInfo() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Doctor currentDoctor = dataSnapshot.getValue(Doctor.class);
                    if (currentDoctor != null) {
                        etName.setText(currentDoctor.getName());
                        etUsername.setText(currentDoctor.getUsername());
                        etEmail.setText(currentDoctor.getEmail());
                        etPhoneNumber.setText(currentDoctor.getPhone());
                        etAddress.setText(currentDoctor.getAddress());
                        etPostalCode.setText(currentDoctor.getPostalCode());
                        etBio.setText(currentDoctor.getBio());
                        etSpecialization.setText(currentDoctor.getSpecialization());
                        etExperience.setText(currentDoctor.getExperience());
                        etQualification.setText(currentDoctor.getQualification());

                        // Load the profile image
                        String currentImageBase64 = currentDoctor.getProfileImageBase64();
                        if (currentImageBase64 != null && !currentImageBase64.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(currentImageBase64, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ivProfileImage.setImageBitmap(decodedByte);
                            } catch (Exception e) {
                                ivProfileImage.setImageResource(R.drawable.doctor_1); // Default image on error
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(edit_profile_doctor.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDoctorProfile() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name cannot be empty.");
            return;
        }

        Map<String, Object> profileUpdates = new HashMap<>();
        profileUpdates.put("name", name);
        profileUpdates.put("username", etUsername.getText().toString().trim());
        profileUpdates.put("email", etEmail.getText().toString().trim());
        profileUpdates.put("phone", etPhoneNumber.getText().toString().trim());
        profileUpdates.put("address", etAddress.getText().toString().trim());
        profileUpdates.put("postalCode", etPostalCode.getText().toString().trim());
        profileUpdates.put("bio", etBio.getText().toString().trim());
        profileUpdates.put("specialization", etSpecialization.getText().toString().trim());
        profileUpdates.put("experience", etExperience.getText().toString().trim());
        profileUpdates.put("qualification", etQualification.getText().toString().trim());

        // If a new image was selected, add it to the updates
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            profileUpdates.put("profileImageBase64", imageBase64);
        }

        databaseReference.updateChildren(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(edit_profile_doctor.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(edit_profile_doctor.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Image Handling Methods ---

    private void initializeLaunchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    Bitmap resizedBitmap = resizeBitmap(bitmap, 512); // Resize for efficiency
                    imageBase64 = bitmapToBase64(resizedBitmap);
                    ivProfileImage.setImageBitmap(resizedBitmap);
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkPermissionAndOpenGallery() {
        String permission;
        // Corrected the version check to use the integer value for API 34
        if (Build.VERSION.SDK_INT >= 34) { // Android 14 and above
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}

