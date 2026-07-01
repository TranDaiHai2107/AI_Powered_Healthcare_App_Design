package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private FirebaseFirestore db;
    private DoctorAdapter doctorAdapter;
    private HospitalAdapter hospitalAdapter;
    private boolean showingDoctors = true;
    private String currentQuery = "";
    private String filterSpecialty = "All";
    private int filterMaxPrice = 500;
    private boolean sortByRating = true;

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
        setupFilter();
        setupBottomNav();
        loadAllDoctors();
        loadAllHospitals();
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Doctors"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Hospitals"));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                showingDoctors = tab.getPosition() == 0;
                updateRecyclerView();
                applyFiltersAndSort();
            }
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
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                applyFiltersAndSort();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFiltersAndSort() {
        String lowerQuery = currentQuery.toLowerCase(Locale.US);
        String lowerSpecialty = filterSpecialty.equalsIgnoreCase("All") ? null : filterSpecialty.toLowerCase(Locale.US);

        List<Doctor> filteredDoctors = new ArrayList<>();
        for (Doctor d : allDoctors) {
            boolean matchesSearch = matchesDoctorSearch(d, lowerQuery);
            boolean matchesSpecialty = lowerSpecialty == null || (d.getSpecialization() != null && d.getSpecialization().toLowerCase(Locale.US).contains(lowerSpecialty));
            boolean matchesPrice = d.getConsultationFee() != null && d.getConsultationFee() <= filterMaxPrice;
            if (matchesSearch && matchesSpecialty && matchesPrice) {
                filteredDoctors.add(d);
            }
        }

        if (sortByRating) {
            Collections.sort(filteredDoctors, (d1, d2) -> Double.compare(value(d2.getRating()), value(d1.getRating())));
        } else {
            Collections.sort(filteredDoctors, (d1, d2) -> Double.compare(value(d1.getConsultationFee()), value(d2.getConsultationFee())));
        }

        List<Hospital> filteredHospitals = new ArrayList<>();
        for (Hospital h : allHospitals) {
            boolean matchesSearch = matchesHospitalSearch(h, lowerQuery);
            boolean matchesSpecialty = lowerSpecialty == null || (h.getSpecialties() != null && h.getSpecialties().toLowerCase(Locale.US).contains(lowerSpecialty));
            if (matchesSearch && matchesSpecialty) {
                filteredHospitals.add(h);
            }
        }

        if (sortByRating) {
            Collections.sort(filteredHospitals, (h1, h2) -> Double.compare(value(h2.getRating()), value(h1.getRating())));
        }

        doctorAdapter.updateList(filteredDoctors);
        hospitalAdapter.updateList(filteredHospitals);
    }

    private boolean matchesDoctorSearch(Doctor doctor, String query) {
        if (query.isEmpty()) return true;
        return contains(doctor.getName(), query)
                || contains(doctor.getSpecialization(), query)
                || contains(doctor.getHospitalName(), query)
                || contains(doctor.getBio(), query);
    }

    private boolean matchesHospitalSearch(Hospital hospital, String query) {
        if (query.isEmpty()) return true;
        return contains(hospital.getName(), query)
                || contains(hospital.getSpecialties(), query)
                || contains(hospital.getAddress(), query)
                || contains(hospital.getPriceRange(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.US).contains(query);
    }

    private double value(Double value) {
        return value != null ? value : 0;
    }

    private void setupFilter() {
        binding.btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        dialog.setContentView(view);

        ImageButton btnClose = view.findViewById(R.id.btnClose);
        RadioGroup rgSort = view.findViewById(R.id.rgSort);
        RadioButton rbSortRating = view.findViewById(R.id.rbSortRating);
        RadioButton rbSortPrice = view.findViewById(R.id.rbSortPrice);
        Spinner spinnerSpecialty = view.findViewById(R.id.spinnerSpecialty);
        SeekBar seekBarPrice = view.findViewById(R.id.seekBarPrice);
        TextView tvPriceValue = view.findViewById(R.id.tvPriceValue);
        Button btnReset = view.findViewById(R.id.btnReset);
        Button btnApply = view.findViewById(R.id.btnApply);

        // Setup Spinner
        String[] specialties = {"All", "Cardiologist", "Dentist", "Dermatologist", "Pediatrician", "Neurologist", "General Physician"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, specialties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialty.setAdapter(adapter);

        // Restore currently selected values
        if (sortByRating) {
            rbSortRating.setChecked(true);
        } else {
            rbSortPrice.setChecked(true);
        }

        int specialtyIndex = 0;
        for (int i = 0; i < specialties.length; i++) {
            if (specialties[i].equalsIgnoreCase(filterSpecialty)) {
                specialtyIndex = i;
                break;
            }
        }
        spinnerSpecialty.setSelection(specialtyIndex);

        seekBarPrice.setProgress(filterMaxPrice);
        tvPriceValue.setText("$" + filterMaxPrice);

        seekBarPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { tvPriceValue.setText("$" + progress); }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnReset.setOnClickListener(v -> {
            filterSpecialty = "All";
            filterMaxPrice = 500;
            sortByRating = true;
            applyFiltersAndSort();
            dialog.dismiss();
        });
        btnApply.setOnClickListener(v -> {
            filterSpecialty = spinnerSpecialty.getSelectedItem().toString();
            filterMaxPrice = seekBarPrice.getProgress();
            sortByRating = rbSortRating.isChecked();
            applyFiltersAndSort();
            dialog.dismiss();
        });

        dialog.show();
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
                    applyFiltersAndSort();
                });
    }

    private void loadAllHospitals() {
        db.collection("hospitals").get()
                .addOnSuccessListener(querySnapshot -> {
                    allHospitals.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        allHospitals.add(doc.toObject(Hospital.class));
                    }
                    applyFiltersAndSort();
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
