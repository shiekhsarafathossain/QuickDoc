package com.ewucsesummer25.quickdoc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class portal_patient extends AppCompatActivity {

    private Button btnLoginPatient, btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal_patient);

        btnLoginPatient = findViewById(R.id.btnLoginPatient);
        btnSignup = findViewById(R.id.btnSignup);

        // Set listener to go to the Patient Login page
        btnLoginPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(portal_patient.this, Patient_Login.class);
                startActivity(i);
            }
        });

        // Set listener to go to the Patient Sign Up page
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(portal_patient.this, Sign_up.class);
                startActivity(i);
            }
        });
    }
}
