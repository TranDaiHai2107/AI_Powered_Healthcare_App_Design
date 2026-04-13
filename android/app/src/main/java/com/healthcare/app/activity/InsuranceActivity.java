package com.healthcare.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityInsuranceBinding;
import com.healthcare.app.model.Insurance;

public class InsuranceActivity extends AppCompatActivity {

    private ActivityInsuranceBinding binding;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInsuranceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        applyBadgeStyles();
        setupClickListeners();
        setupBottomNav();
        loadInsurance();
    }

    private void applyBadgeStyles() {
        GradientDrawable statusBadgeBg = new GradientDrawable();
        statusBadgeBg.setShape(GradientDrawable.RECTANGLE);
        statusBadgeBg.setCornerRadius(20f);
        statusBadgeBg.setColor(ContextCompat.getColor(this, R.color.pastel_mint_20));
        binding.tvStatusBadge.setBackground(statusBadgeBg);
    }

    private void setupClickListeners() { binding.btnBack.setOnClickListener(v -> finish()); }

    private void loadInsurance() {
        db.collection("insurance").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Insurance insurance = doc.toObject(Insurance.class);
                        populateInsurance(insurance);
                        break;
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load insurance", Toast.LENGTH_SHORT).show());
    }

    private void populateInsurance(Insurance insurance) {
        binding.tvProvider.setText(insurance.getProvider());
        binding.tvPolicyNumber.setText("Policy: " + insurance.getPolicyNumber());
        binding.tvCoverage.setText(insurance.getCoverage());
        binding.tvValidUntil.setText(insurance.getValidUntil());
        if (insurance.getStatus() != null) binding.tvStatusBadge.setText(insurance.getStatus());
    }

    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { startActivity(new Intent(this, HomeActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_search) { startActivity(new Intent(this, SearchActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_appointments) { startActivity(new Intent(this, AppointmentsActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_records) { startActivity(new Intent(this, MedicalRecordsActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_profile) { startActivity(new Intent(this, ProfileActivity.class)); overridePendingTransition(0, 0); return true; }
            return false;
        });
    }
}
