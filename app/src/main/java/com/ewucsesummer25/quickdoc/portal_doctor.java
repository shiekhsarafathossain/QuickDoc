package com.ewucsesummer25.quickdoc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class portal_doctor extends AppCompatActivity {
    private Button btnLoginDoctor, btnSignup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal_doctor);

        btnLoginDoctor = findViewById(R.id.btnLoginDoctor);
        btnSignup = findViewById(R.id.btnSignup);

        btnLoginDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(portal_doctor.this, Doctor_Login.class);
                startActivity(i);
                finishAffinity();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(portal_doctor.this, signup_doctor.class);
                startActivity(i);
                finishAffinity();

            }
        });

    }
}