package com.healthcare.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityProfileBinding;
import com.healthcare.app.model.User;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        styleAvatar();
        styleMenuIcons();
        setupClickListeners();
        setupBottomNav();
        loadProfile();
    }

    private void styleAvatar() {
        GradientDrawable avatarBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{ ContextCompat.getColor(this, R.color.pastel_blue), ContextCompat.getColor(this, R.color.pastel_lavender) });
        avatarBg.setShape(GradientDrawable.OVAL);
        binding.viewAvatar.setBackground(avatarBg);
    }

    private void styleMenuIcons() {
        int iconBgColor = ContextCompat.getColor(this, R.color.pastel_blue_10);
        int[] iconContainerIds = { R.id.menuPersonalInfo, R.id.menuFamilyMembers, R.id.menuInsurance, R.id.menuPaymentMethods, R.id.menuAppSettings, R.id.menuHelpCenter, R.id.menuContactSupport };
        for (int id : iconContainerIds) {
            android.view.View menuRow = findViewById(id);
            if (menuRow != null) {
                android.widget.FrameLayout iconFrame = (android.widget.FrameLayout) ((android.widget.LinearLayout) menuRow).getChildAt(0);
                GradientDrawable bg = new GradientDrawable(); bg.setShape(GradientDrawable.OVAL); bg.setColor(iconBgColor); iconFrame.setBackground(bg);
            }
        }
    }

    private void setupClickListeners() {
        binding.menuInsurance.setOnClickListener(v -> startActivity(new Intent(this, InsuranceActivity.class)));
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent); finish();
        });
    }

    private void loadProfile() {
        if (userId.isEmpty()) return;
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) { User user = doc.toObject(User.class); if (user != null) populateProfile(user); }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void populateProfile(User user) {
        binding.tvName.setText(user.getName());
        binding.tvPatientId.setText("Patient ID: " + (user.getPatientId() != null ? user.getPatientId() : "N/A"));
        binding.tvEmail.setText(user.getEmail());
        binding.tvPhone.setText(user.getPhone());
        binding.tvAddress.setText(user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "Not set");
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_profile);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { startActivity(new Intent(this, HomeActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_search) { startActivity(new Intent(this, SearchActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_appointments) { startActivity(new Intent(this, AppointmentsActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_records) { startActivity(new Intent(this, MedicalRecordsActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_profile) return true;
            return false;
        });
    }
}
