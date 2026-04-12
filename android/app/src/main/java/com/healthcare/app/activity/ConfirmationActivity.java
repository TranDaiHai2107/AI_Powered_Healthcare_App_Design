package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.healthcare.app.databinding.ActivityConfirmationBinding;
import com.healthcare.app.model.Doctor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ConfirmationActivity extends AppCompatActivity {

    private ActivityConfirmationBinding binding;
    private FirebaseFirestore db;
    private String doctorId;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        doctorId = getIntent().getStringExtra("doctorId");
        total = getIntent().getDoubleExtra("total", 0);

        setupBookingDetails();
        setupClickListeners();
        loadDoctor();
    }

    private void setupBookingDetails() {
        String bookingId = "HC-" + new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date()) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.US);
        binding.tvBookingId.setText(bookingId);
        binding.tvDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date()));
        binding.tvTime.setText(new SimpleDateFormat("hh:mm a", Locale.US).format(new Date()));
        binding.tvTotal.setText(String.format(Locale.US, "$%.0f", total));
    }

    private void loadDoctor() {
        if (doctorId == null) return;
        db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) { Doctor doctor = doc.toObject(Doctor.class); if (doctor != null) binding.tvDoctor.setText(doctor.getName()); }
                    else binding.tvDoctor.setText("—");
                })
                .addOnFailureListener(e -> binding.tvDoctor.setText("—"));
    }

    private void setupClickListeners() {
        binding.btnDownloadReceipt.setOnClickListener(v -> Toast.makeText(this, "Downloading receipt...", Toast.LENGTH_SHORT).show());
        binding.btnViewAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, AppointmentsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent); finish();
        });
        binding.btnBackToHome.setOnClickListener(v -> navigateToHome());
    }

    @SuppressWarnings("deprecation")
    @Override public void onBackPressed() { navigateToHome(); }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent); finish();
    }
}
