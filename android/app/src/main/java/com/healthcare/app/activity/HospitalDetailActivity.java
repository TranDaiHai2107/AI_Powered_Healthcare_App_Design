package com.healthcare.app.activity;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.healthcare.app.R;
import com.healthcare.app.adapter.DoctorAdapter;
import com.healthcare.app.databinding.ActivityHospitalDetailBinding;
import com.healthcare.app.model.Doctor;
import com.healthcare.app.model.Hospital;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HospitalDetailActivity extends AppCompatActivity {

    private ActivityHospitalDetailBinding binding;
    private FirebaseFirestore db;
    private String hospitalId;
    private Hospital currentHospital;
    private DoctorAdapter doctorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHospitalDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        hospitalId = getIntent().getStringExtra("hospitalId");

        setupDoctorsList();
        setupClickListeners();
        loadHospital();
    }

    private void setupDoctorsList() {
        doctorAdapter = new DoctorAdapter(this, new ArrayList<>(), doctor -> {
            Intent intent = new Intent(this, DoctorDetailActivity.class);
            intent.putExtra("doctorId", doctor.getDocumentId());
            startActivity(intent);
        });
        binding.rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDoctors.setNestedScrollingEnabled(false);
        binding.rvDoctors.setAdapter(doctorAdapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBookAppointment.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        binding.btnCall.setOnClickListener(v -> {
            if (currentHospital != null && currentHospital.getPhone() != null) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + currentHospital.getPhone().trim()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Calling hospital...", Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnDirection.setOnClickListener(v -> openDirections());
        binding.cardMap.setOnClickListener(v -> openDirections());
        binding.tvSeeAllDoctors.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
    }

    private void openDirections() {
        if (currentHospital != null && currentHospital.getLatitude() != null && currentHospital.getLongitude() != null) {
            double lat = currentHospital.getLatitude();
            double lon = currentHospital.getLongitude();
            String name = currentHospital.getName();
            
            String uriString = String.format(Locale.US, "geo:%f,%f?q=%f,%f(%s)", lat, lon, lat, lon, Uri.encode(name));
            Uri intentUri = Uri.parse(uriString);
            
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, intentUri);
                startActivity(fallbackIntent);
            }
        } else {
            Toast.makeText(this, "Location coordinates not available for this hospital", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadHospital() {
        if (hospitalId == null) { Toast.makeText(this, "Invalid hospital", Toast.LENGTH_SHORT).show(); finish(); return; }

        db.collection("hospitals").document(hospitalId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentHospital = doc.toObject(Hospital.class);
                        if (currentHospital != null) {
                            populateUI(currentHospital);
                            loadDoctors();
                        }
                    } else {
                        Toast.makeText(this, "Hospital not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load hospital details", Toast.LENGTH_SHORT).show());
    }

    private void populateUI(Hospital hospital) {
        binding.tvHospitalName.setText(hospital.getName());
        binding.tvAddress.setText(hospital.getAddress() != null ? hospital.getAddress() : "Address not available");
        if (hospital.getRating() != null) binding.tvRating.setText(String.format(Locale.US, "%.1f", hospital.getRating()));
        if (hospital.getReviewCount() != null) binding.tvReviewCount.setText(String.format(Locale.US, "(%d)", hospital.getReviewCount()));
        binding.tvOperatingHours.setText(hospital.getOperatingHours() != null ? hospital.getOperatingHours() : "N/A");
        binding.tvDistance.setText(hospital.getDistance() != null ? hospital.getDistance() : "N/A");
        Glide.with(this).load(hospital.getImage()).centerCrop().placeholder(R.drawable.bg_rounded_image).into(binding.imgHospital);
        populateSpecialties(hospital.getSpecialtiesArray());
        setupInlineMap(hospital);
    }

    private void setupInlineMap(Hospital hospital) {
        if (hospital.getLatitude() != null && hospital.getLongitude() != null) {
            double lat = hospital.getLatitude();
            double lon = hospital.getLongitude();
            
            double delta = 0.005;
            double minLon = lon - delta;
            double minLat = lat - delta;
            double maxLon = lon + delta;
            double maxLat = lat + delta;
            
            String mapUrl = String.format(Locale.US, 
                "https://www.openstreetmap.org/export/embed.html?bbox=%f,%f,%f,%f&layer=mapnik&marker=%f,%f",
                minLon, minLat, maxLon, maxLat, lat, lon
            );
            
            binding.webViewMap.getSettings().setJavaScriptEnabled(true);
            binding.webViewMap.setWebViewClient(new android.webkit.WebViewClient() {
                @Override
                public void onPageFinished(android.webkit.WebView view, String url) {
                    binding.webViewMap.setVisibility(View.VISIBLE);
                    binding.layoutMapPlaceholder.setVisibility(View.GONE);
                }
            });
            binding.webViewMap.loadUrl(mapUrl);
        } else {
            binding.webViewMap.setVisibility(View.GONE);
            binding.layoutMapPlaceholder.setVisibility(View.VISIBLE);
            binding.tvMapPlaceholder.setText("Map not available");
        }
    }

    private void populateSpecialties(String[] specialties) {
        binding.chipGroupSpecialties.removeAllViews();
        if (specialties == null) return;
        for (String specialty : specialties) {
            String trimmed = specialty.trim();
            if (!trimmed.isEmpty()) {
                Chip chip = new Chip(this);
                chip.setText(trimmed); chip.setTextSize(13);
                chip.setChipBackgroundColorResource(R.color.pastel_blue_10);
                chip.setTextColor(getResources().getColor(R.color.healthcare_dark, null));
                chip.setClickable(false); chip.setCheckable(false);
                binding.chipGroupSpecialties.addView(chip);
            }
        }
    }

    private void loadDoctors() {
        db.collection("doctors").whereEqualTo("hospitalId", hospitalId).get()
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
