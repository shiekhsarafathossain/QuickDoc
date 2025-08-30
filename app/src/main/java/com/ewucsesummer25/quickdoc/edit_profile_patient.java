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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class edit_profile_patient extends AppCompatActivity {

    private EditText etName, etUsername, etEmail, etPhone, etAddress;
    private Button btnBack, btnDone;
    private ImageView ivProfileImage; // For the profile picture
    private DatabaseReference patientRef;
    private String patientId;
    private String imageBase64; // To hold the image data

    // Launchers for handling permissions and image selection
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_patient);

        patientId = getIntent().getStringExtra("PATIENT_ID");

        initializeViews();
        initializeLaunchers(); // Set up the activity result launchers
        setupButtonClickListeners();


        if (patientId != null && !patientId.isEmpty()) {
            patientRef = FirebaseDatabase.getInstance().getReference("patients").child(patientId);
            loadPatientData();
        } else {
            Toast.makeText(this, "Error: Patient ID is missing.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        etName = findViewById(R.id.etEditName);
        etUsername = findViewById(R.id.etEditUserame);
        etEmail = findViewById(R.id.etEditEmail);
        etPhone = findViewById(R.id.etChangePhoneNumber);
        etAddress = findViewById(R.id.etChangeAddress);
        btnBack = findViewById(R.id.btnBack);
        btnDone = findViewById(R.id.btnDone);
        ivProfileImage = findViewById(R.id.imgUser); // Initialize ImageView
    }

    private void setupButtonClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePatientProfile();
            }
        });

        // Make the profile image clickable to change it
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndOpenGallery();
            }
        });
    }


    private void loadPatientData() {
        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Patient patient = snapshot.getValue(Patient.class);
                    if (patient != null) {
                        etName.setText(patient.getName());
                        etUsername.setText(patient.getUsername());
                        etEmail.setText(patient.getEmail());
                        etPhone.setText(patient.getPhone());
                        etAddress.setText(patient.getAddress());

                        // Load and display the profile image if it exists
                        imageBase64 = patient.getProfileImageBase64();
                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                            byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ivProfileImage.setImageBitmap(decodedByte);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(edit_profile_patient.this, "Failed to load profile data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePatientProfile() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required.");
            etName.requestFocus();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("username", username);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("address", address);
        // Include the profile image in the update
        updates.put("profileImageBase64", imageBase64);


        patientRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(edit_profile_patient.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(edit_profile_patient.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Methods for Image Picking and Processing ---

    private void initializeLaunchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied to access gallery.", Toast.LENGTH_SHORT).show();
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    Bitmap resizedBitmap = resizeBitmap(bitmap, 512); // Resize for efficiency
                    imageBase64 = bitmapToBase64(resizedBitmap); // Convert to Base64
                    ivProfileImage.setImageBitmap(resizedBitmap); // Display the new image
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkPermissionAndOpenGallery() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

