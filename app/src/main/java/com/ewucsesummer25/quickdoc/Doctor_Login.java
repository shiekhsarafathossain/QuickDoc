package com.ewucsesummer25.quickdoc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Doctor_Login extends AppCompatActivity {

    private EditText etEmail, etPass;
    private Button btnBack, btnLogin;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login);

        databaseReference = FirebaseDatabase.getInstance().getReference("doctors");

        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        btnBack = findViewById(R.id.btnBack);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginDoctor();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }

    private void loginDoctor() {
        String email = etEmail.getText().toString().trim();
        String password = etPass.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPass.setError("Password is required");
            return;
        }

        Query checkUserQuery = databaseReference.orderByChild("email").equalTo(email);

        checkUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String passwordFromDB = userSnapshot.child("password").getValue(String.class);

                        if (passwordFromDB != null && passwordFromDB.equals(password)) {
                            String doctorId = userSnapshot.getKey();



                            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("USER_ID", doctorId);
                            editor.putString("USER_TYPE", "doctor");
                            editor.apply();


                            Toast.makeText(Doctor_Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Doctor_Login.this, doctor_dashboard.class);
                            intent.putExtra("DOCTOR_ID", doctorId);
                            startActivity(intent);
                            finishAffinity();
                            return;
                        }
                    }
                    etPass.setError("Wrong Password");
                    etPass.requestFocus();
                } else {
                    etEmail.setError("User with this email does not exist");
                    etEmail.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Doctor_Login.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

