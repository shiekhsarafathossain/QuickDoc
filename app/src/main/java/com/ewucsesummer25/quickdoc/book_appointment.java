package com.ewucsesummer25.quickdoc;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class book_appointment extends AppCompatActivity {

    private Spinner spinnerDoctors;
    private CalendarView calendarView;
    private ListView lvTimeSlot;
    private Button btnBack, btnDone;

    private DatabaseReference doctorsRef, appointmentsRef;
    private List<Doctor> doctorList;
    private List<String> doctorNames;
    private List<String> allTimeSlots;
    private List<String> bookedTimeSlots;
    private ArrayAdapter<String> doctorAdapter;
    private TimeSlotAdapter timeSlotAdapter;

    private String selectedDoctorId, selectedDoctorName, selectedDate, selectedTimeSlot, patientId, patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_appointment);

        patientId = getIntent().getStringExtra("PATIENT_ID");
        patientName = getIntent().getStringExtra("PATIENT_NAME");

        doctorsRef = FirebaseDatabase.getInstance().getReference("doctors");
        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        spinnerDoctors = findViewById(R.id.spinnerDoctors);
        calendarView = findViewById(R.id.cvDate);
        lvTimeSlot = findViewById(R.id.lvTimeSlot);
        btnBack = findViewById(R.id.btnBack);
        btnDone = findViewById(R.id.btnDone);

        doctorList = new ArrayList<>();
        doctorNames = new ArrayList<>();
        bookedTimeSlots = new ArrayList<>();
        allTimeSlots = new ArrayList<>(Arrays.asList("09:00 AM - 09:30 AM", "10:00 AM - 10:30 AM", "11:00 AM - 11:30 AM", "02:00 PM - 02:30 PM", "03:00 PM - 03:30 PM"));

        setupDoctorSpinner();
        setupTimeSlotListView();
        setupCalendarView();
        loadDoctors();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookAppointment();
            }
        });
    }

    private void setupDoctorSpinner() {
        doctorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctorNames);
        doctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctors.setAdapter(doctorAdapter);

        spinnerDoctors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedDoctorId = doctorList.get(position).getDoctorId();

                selectedDoctorName = doctorList.get(position).getName();
                fetchBookedSlotsForDoctorAndDate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDoctorId = null;
                selectedDoctorName = null;
            }
        });
    }

    private void setupTimeSlotListView() {
        timeSlotAdapter = new TimeSlotAdapter(this, android.R.layout.simple_list_item_single_choice, allTimeSlots, bookedTimeSlots);
        lvTimeSlot.setAdapter(timeSlotAdapter);
        lvTimeSlot.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        lvTimeSlot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedSlot = allTimeSlots.get(position);
                if (bookedTimeSlots.contains(clickedSlot)) {
                    Toast.makeText(book_appointment.this, "This time slot is unavailable.", Toast.LENGTH_SHORT).show();
                    lvTimeSlot.setItemChecked(position, false);
                    selectedTimeSlot = null;
                } else {
                    selectedTimeSlot = clickedSlot;
                }
            }
        });
    }

    private void setupCalendarView() {
        calendarView.setMinDate(System.currentTimeMillis() - 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(Calendar.getInstance().getTime());

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = year + "-" + String.format(Locale.getDefault(), "%02d", month + 1) + "-" + String.format(Locale.getDefault(), "%02d", dayOfMonth);
                fetchBookedSlotsForDoctorAndDate();
            }
        });
    }

    private void loadDoctors() {
        doctorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                doctorList.clear();
                doctorNames.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        doctor.setDoctorId(snapshot.getKey());
                        doctorList.add(doctor);
                        doctorNames.add(doctor.getName() + " (" + doctor.getSpecialization() + ")");
                    }
                }
                doctorAdapter.notifyDataSetChanged();
                if (!doctorList.isEmpty()) {
                    spinnerDoctors.setSelection(0);
                    selectedDoctorId = doctorList.get(0).getDoctorId();
                    fetchBookedSlotsForDoctorAndDate();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(book_appointment.this, "Failed to load doctors.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBookedSlotsForDoctorAndDate() {
        if (selectedDoctorId == null || selectedDate == null) return;

        Query query = appointmentsRef.orderByChild("doctorId").equalTo(selectedDoctorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookedTimeSlots.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Appointment appointment = snapshot.getValue(Appointment.class);
                    if (appointment != null && appointment.getAppointmentDate().equals(selectedDate)) {
                        bookedTimeSlots.add(appointment.getAppointmentTime());
                    }
                }
                timeSlotAdapter.notifyDataSetChanged();
                lvTimeSlot.clearChoices();
                selectedTimeSlot = null;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(book_appointment.this, "Failed to check availability.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bookAppointment() {
        if (patientId == null || patientName == null) {
            Toast.makeText(this, "Patient details not found. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }
        if (selectedDoctorId == null || selectedDoctorName == null) {
            Toast.makeText(this, "Please select a doctor.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTimeSlot == null) {
            Toast.makeText(this, "Please select an available time slot.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bookedTimeSlots.contains(selectedTimeSlot)) {
            Toast.makeText(this, "This time slot is already booked. Please select another.", Toast.LENGTH_LONG).show();
            return;
        }

        String appointmentId = appointmentsRef.push().getKey();
        Appointment appointment = new Appointment(appointmentId, patientId, selectedDoctorId, selectedDoctorName, patientName, selectedDate, selectedTimeSlot, "Scheduled");

        if (appointmentId != null) {
            appointmentsRef.child(appointmentId).setValue(appointment).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(book_appointment.this, "Appointment booked successfully!", Toast.LENGTH_LONG).show();
                    fetchBookedSlotsForDoctorAndDate();
                } else {
                    Toast.makeText(book_appointment.this, "Failed to book appointment. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private class TimeSlotAdapter extends ArrayAdapter<String> {

        private final List<String> slots;
        private final List<String> bookedSlots;

        public TimeSlotAdapter(@NonNull Context context, int resource, @NonNull List<String> slots, List<String> bookedSlots) {
            super(context, resource, slots);
            this.slots = slots;
            this.bookedSlots = bookedSlots;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            String timeSlot = slots.get(position);

            if (bookedSlots.contains(timeSlot)) {
                textView.setTextColor(Color.RED);
                textView.setEnabled(false);
            } else {
                textView.setTextColor(Color.BLACK);
                textView.setEnabled(true);
            }


            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            String timeSlot = slots.get(position);
            return !bookedSlots.contains(timeSlot);
        }
    }
}

