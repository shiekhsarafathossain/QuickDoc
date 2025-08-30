package com.ewucsesummer25.quickdoc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class portal extends AppCompatActivity {
    private Button btnDoctor, btnPatient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.portal);

        btnDoctor = findViewById(R.id.btnDoctorPortal);
        btnPatient = findViewById(R.id.btnPatientPortal);

        btnPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(portal.this, portal_patient.class);
                startActivity(i);
                finishAffinity();
            }
        });

        btnDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(portal.this, portal_doctor.class);
                startActivity(i);
                finishAffinity();

            }
        });





    }
}