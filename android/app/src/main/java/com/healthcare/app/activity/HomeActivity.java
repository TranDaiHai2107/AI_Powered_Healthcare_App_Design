package com.healthcare.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.healthcare.app.R;
import com.healthcare.app.adapter.DoctorAdapter;
import com.healthcare.app.adapter.HospitalAdapter;
import com.healthcare.app.databinding.ActivityHomeBinding;
import com.healthcare.app.model.Doctor;
import com.healthcare.app.model.Hospital;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private FirebaseFirestore db;
    private DoctorAdapter doctorAdapter;
    private HospitalAdapter hospitalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", "User");
        binding.tvUserName.setText(userName);

        setupRecyclerViews();
        setupClickListeners();
        setupBottomNav();
        loadHospitals();
        loadDoctors();
    }

    private void setupRecyclerViews() {
        hospitalAdapter = new HospitalAdapter(this, new ArrayList<>(), hospital -> {
            Intent intent = new Intent(this, HospitalDetailActivity.class);
            intent.putExtra("hospitalId", hospital.getDocumentId());
            startActivity(intent);
        });
        binding.rvHospitals.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHospitals.setAdapter(hospitalAdapter);

        doctorAdapter = new DoctorAdapter(this, new ArrayList<>(), doctor -> {
            Intent intent = new Intent(this, DoctorDetailActivity.class);
            intent.putExtra("doctorId", doctor.getDocumentId());
            startActivity(intent);
        });
        binding.rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDoctors.setAdapter(doctorAdapter);
    }

    private void setupClickListeners() {
        binding.searchBar.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        binding.btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        binding.cardBookAppointment.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        binding.cardMedicalRecords.setOnClickListener(v -> startActivity(new Intent(this, MedicalRecordsActivity.class)));
        binding.cardFindDoctors.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        binding.cardInsurance.setOnClickListener(v -> startActivity(new Intent(this, InsuranceActivity.class)));
        binding.tvSeeAllHospitals.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        binding.tvSeeAllDoctors.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_home);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            else if (id == R.id.nav_search) { startActivity(new Intent(this, SearchActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_appointments) { startActivity(new Intent(this, AppointmentsActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_records) { startActivity(new Intent(this, MedicalRecordsActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_profile) { startActivity(new Intent(this, ProfileActivity.class)); overridePendingTransition(0, 0); return true; }
            return false;
        });
    }

    private void loadHospitals() {
        db.collection("hospitals").limit(2).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Hospital> hospitals = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        hospitals.add(doc.toObject(Hospital.class));
                    }
                    hospitalAdapter.updateList(hospitals);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load hospitals", Toast.LENGTH_SHORT).show());
    }

    private void loadDoctors() {
        db.collection("doctors").limit(3).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Doctor> doctors = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doctors.add(doc.toObject(Doctor.class));
                    }
                    doctorAdapter.updateList(doctors);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load doctors", Toast.LENGTH_SHORT).show());
    }
}
