package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.healthcare.app.R;
import com.healthcare.app.adapter.DoctorAdapter;
import com.healthcare.app.adapter.HospitalAdapter;
import com.healthcare.app.databinding.ActivitySearchBinding;
import com.healthcare.app.model.Doctor;
import com.healthcare.app.model.Hospital;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private FirebaseFirestore db;
    private DoctorAdapter doctorAdapter;
    private HospitalAdapter hospitalAdapter;
    private boolean showingDoctors = true;

    private List<Doctor> allDoctors = new ArrayList<>();
    private List<Hospital> allHospitals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setupTabs();
        setupAdapters();
        setupSearch();
        setupChips();
        setupBottomNav();
        loadAllDoctors();
        loadAllHospitals();
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Doctors"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Hospitals"));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { showingDoctors = tab.getPosition() == 0; updateRecyclerView(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupAdapters() {
        doctorAdapter = new DoctorAdapter(this, new ArrayList<>(), doctor -> {
            Intent intent = new Intent(this, DoctorDetailActivity.class);
            intent.putExtra("doctorId", doctor.getDocumentId());
            startActivity(intent);
        });
        hospitalAdapter = new HospitalAdapter(this, new ArrayList<>(), hospital -> {
            Intent intent = new Intent(this, HospitalDetailActivity.class);
            intent.putExtra("hospitalId", hospital.getDocumentId());
            startActivity(intent);
        });
        binding.rvResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvResults.setAdapter(doctorAdapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterResults(s.toString().trim()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupChips() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            for (int i = 0; i < group.getChildCount(); i++) {
                Chip chip = (Chip) group.getChildAt(i);
                chip.setChipBackgroundColorResource(checkedIds.contains(chip.getId()) ? R.color.pastel_blue : R.color.healthcare_gray);
            }
        });
    }

    private void filterResults(String query) {
        if (query.isEmpty()) {
            doctorAdapter.updateList(allDoctors);
            hospitalAdapter.updateList(allHospitals);
            return;
        }
        String lower = query.toLowerCase(Locale.US);

        List<Doctor> filteredDoctors = new ArrayList<>();
        for (Doctor d : allDoctors) {
            if ((d.getName() != null && d.getName().toLowerCase(Locale.US).contains(lower)) ||
                (d.getSpecialization() != null && d.getSpecialization().toLowerCase(Locale.US).contains(lower))) {
                filteredDoctors.add(d);
            }
        }
        doctorAdapter.updateList(filteredDoctors);

        List<Hospital> filteredHospitals = new ArrayList<>();
        for (Hospital h : allHospitals) {
            if ((h.getName() != null && h.getName().toLowerCase(Locale.US).contains(lower)) ||
                (h.getSpecialties() != null && h.getSpecialties().toLowerCase(Locale.US).contains(lower))) {
                filteredHospitals.add(h);
            }
        }
        hospitalAdapter.updateList(filteredHospitals);
    }

    private void updateRecyclerView() {
        binding.rvResults.setAdapter(showingDoctors ? doctorAdapter : hospitalAdapter);
    }

    private void loadAllDoctors() {
        db.collection("doctors").get()
                .addOnSuccessListener(querySnapshot -> {
                    allDoctors.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        allDoctors.add(doc.toObject(Doctor.class));
                    }
                    doctorAdapter.updateList(allDoctors);
                });
    }

    private void loadAllHospitals() {
        db.collection("hospitals").get()
                .addOnSuccessListener(querySnapshot -> {
                    allHospitals.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        allHospitals.add(doc.toObject(Hospital.class));
                    }
                    hospitalAdapter.updateList(allHospitals);
                });
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_search);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { startActivity(new Intent(this, HomeActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_search) return true;
            else if (id == R.id.nav_appointments) { startActivity(new Intent(this, AppointmentsActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_records) { startActivity(new Intent(this, MedicalRecordsActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_profile) { startActivity(new Intent(this, ProfileActivity.class)); overridePendingTransition(0, 0); return true; }
            return false;
        });
    }
}
