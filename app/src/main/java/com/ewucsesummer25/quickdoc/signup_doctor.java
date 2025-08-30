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
import android.util.Patterns;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class signup_doctor extends AppCompatActivity {

    private EditText etName, etUsername, etEmail, etPassword, etRePassword, etAddress,
            etPhoneNumber, etPostalCode, etBio, etSpecialization, etExperience, etQualification;
    private Button btnBack, btnRegister;
    private ImageView ivProfileImage; // For the profile picture
    private String imageBase64; // To hold the image data

    private DatabaseReference databaseReference;

    // Launchers for handling permissions and image selection
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_doctor);

        databaseReference = FirebaseDatabase.getInstance().getReference("doctors");

        initializeViews();
        initializeLaunchers();
        setupButtonClickListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etpassword);
        etRePassword = findViewById(R.id.etRePassword);
        etAddress = findViewById(R.id.etAddress);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etPostalCode = findViewById(R.id.etPostalCode);
        etBio = findViewById(R.id.etBio);
        etSpecialization = findViewById(R.id.etSpecialization);
        etExperience = findViewById(R.id.etExperience);
        etQualification = findViewById(R.id.etQualification);
        btnRegister = findViewById(R.id.btnDone);
        btnBack = findViewById(R.id.btnBack);
        ivProfileImage = findViewById(R.id.addImage); // Initialize ImageView
    }

    private void setupButtonClickListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDoctor();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(signup_doctor.this, portal_doctor.class);
                startActivity(i);
                finishAffinity();
            }
        });

        // Make the profile image clickable to open the gallery
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndOpenGallery();
            }
        });
    }

    private void registerDoctor() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String rePassword = etRePassword.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String specialization = etSpecialization.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String qualification = etQualification.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address.");
            etEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters.");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(rePassword)) {
            etRePassword.setError("Passwords do not match.");
            etRePassword.requestFocus();
            return;
        }

        Doctor newDoctor = new Doctor(name, username, email, password, address, phone, postalCode, bio, specialization, experience, qualification);

        // Add the profile image if one was selected
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            newDoctor.setProfileImageBase64(imageBase64);
        }

        String doctorId = databaseReference.push().getKey();
        newDoctor.setDoctorId(doctorId);

        if (doctorId != null) {
            databaseReference.child(doctorId).setValue(newDoctor).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(signup_doctor.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(signup_doctor.this, Doctor_Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(signup_doctor.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
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
                    Bitmap resizedBitmap = resizeBitmap(bitmap, 512);
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
