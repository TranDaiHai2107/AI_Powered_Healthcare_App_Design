package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityDoctorDetailBinding;
import com.healthcare.app.model.Doctor;

import java.util.Locale;

public class DoctorDetailActivity extends AppCompatActivity {

    private ActivityDoctorDetailBinding binding;
    private FirebaseFirestore db;
    private String doctorId;
    private Doctor currentDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        doctorId = getIntent().getStringExtra("doctorId");

        setupClickListeners();
        loadDoctor();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBookAppointment.setOnClickListener(v -> {
            if (currentDoctor != null) {
                Intent intent = new Intent(this, BookingActivity.class);
                intent.putExtra("doctorId", currentDoctor.getDocumentId());
                startActivity(intent);
            }
        });
        binding.btnCall.setOnClickListener(v -> Toast.makeText(this, "Calling doctor...", Toast.LENGTH_SHORT).show());
        binding.btnMessage.setOnClickListener(v -> Toast.makeText(this, "Opening messages...", Toast.LENGTH_SHORT).show());
    }

    private void loadDoctor() {
        if (doctorId == null) { Toast.makeText(this, "Invalid doctor", Toast.LENGTH_SHORT).show(); finish(); return; }

        db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentDoctor = doc.toObject(Doctor.class);
                        if (currentDoctor != null) populateUI(currentDoctor);
                    } else {
                        Toast.makeText(this, "Doctor not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load doctor details", Toast.LENGTH_SHORT).show());
    }

    private void populateUI(Doctor doctor) {
        binding.tvDoctorName.setText(doctor.getName());
        binding.tvSpecialization.setText(doctor.getSpecialization());
        if (doctor.getRating() != null) binding.tvRating.setText(String.format(Locale.US, "%.1f", doctor.getRating()));
        if (doctor.getReviewCount() != null) binding.tvReviews.setText(String.format(Locale.US, "(%d reviews)", doctor.getReviewCount()));
        if (doctor.getExperience() != null) binding.tvExperience.setText(String.format(Locale.US, "%d yr", doctor.getExperience()));
        binding.tvHospital.setText(doctor.getHospitalName());
        if (doctor.getConsultationFee() != null) binding.tvFee.setText(String.format(Locale.US, "$%.0f", doctor.getConsultationFee()));
        binding.tvBio.setText(doctor.getBio() != null ? doctor.getBio() : "No bio available.");
        Glide.with(this).load(doctor.getImage()).centerCrop().placeholder(R.drawable.bg_rounded_image).into(binding.imgDoctor);
        populateTimeSlots(doctor.getAvailableSlotsArray());
    }

    private void populateTimeSlots(String[] slots) {
        binding.gridTimeSlots.removeAllViews();
        if (slots == null || slots.length == 0) return;

        for (String slot : slots) {
            String trimmed = slot.trim();
            if (trimmed.isEmpty()) continue;
            MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setText(trimmed);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            btn.setCornerRadius(dpToPx(16));
            btn.setTextColor(getResources().getColor(R.color.healthcare_dark, null));
            btn.setStrokeColorResource(R.color.border_color);
            btn.setBackgroundColor(getResources().getColor(R.color.white, null));
            btn.setAllCaps(false);
            btn.setMinHeight(dpToPx(40)); btn.setMinimumHeight(dpToPx(40));
            btn.setPadding(dpToPx(8), 0, dpToPx(8), 0);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0; params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            params.setGravity(Gravity.FILL_HORIZONTAL);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                for (int i = 0; i < binding.gridTimeSlots.getChildCount(); i++) {
                    MaterialButton child = (MaterialButton) binding.gridTimeSlots.getChildAt(i);
                    child.setBackgroundColor(getResources().getColor(R.color.white, null));
                    child.setTextColor(getResources().getColor(R.color.healthcare_dark, null));
                }
                btn.setBackgroundColor(getResources().getColor(R.color.pastel_blue, null));
            });
            binding.gridTimeSlots.addView(btn);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
