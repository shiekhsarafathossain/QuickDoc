package com.ewucsesummer25.quickdoc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class welcome_page extends AppCompatActivity {

    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);


        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = sharedPreferences.getString("USER_ID", null);
        String userType = sharedPreferences.getString("USER_TYPE", null);

        if (userId != null && userType != null) {

            if (userType.equals("patient")) {

                Intent intent = new Intent(welcome_page.this, MainActivity.class);
                intent.putExtra("PATIENT_ID", userId);
                startActivity(intent);
            } else if (userType.equals("doctor")) {
                Intent intent = new Intent(welcome_page.this, doctor_dashboard.class);
                intent.putExtra("DOCTOR_ID", userId);
                startActivity(intent);
            }
            finish();
            return;
        }




        btnGetStarted = findViewById(R.id.btnGetStarted);

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i  = new Intent(welcome_page.this, portal.class);
                startActivity(i);
            }
        });


    }
}
