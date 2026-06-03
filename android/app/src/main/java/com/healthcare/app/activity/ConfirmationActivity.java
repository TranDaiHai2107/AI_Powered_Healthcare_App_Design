package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.healthcare.app.databinding.ActivityConfirmationBinding;

import java.io.File;
import java.util.Locale;

import androidx.core.content.FileProvider;
import android.net.Uri;
import com.healthcare.app.util.ReceiptGenerator;

public class ConfirmationActivity extends AppCompatActivity {

    private ActivityConfirmationBinding binding;
    private String appointmentId;
    private String doctorName;
    private String selectedDate;
    private String selectedTime;
    private String service;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        appointmentId = getIntent().getStringExtra("appointmentId");
        doctorName = getIntent().getStringExtra("doctorName");
        selectedDate = getIntent().getStringExtra("date");
        selectedTime = getIntent().getStringExtra("time");
        service = getIntent().getStringExtra("service");
        total = getIntent().getDoubleExtra("total", 0);

        binding.tvBookingId.setText(appointmentId != null ? appointmentId : "—");
        binding.tvDoctor.setText(doctorName != null ? doctorName : "—");
        binding.tvDate.setText(selectedDate != null ? selectedDate : "—");
        binding.tvTime.setText(selectedTime != null ? selectedTime : "—");
        binding.tvTotal.setText(String.format(Locale.US, "$%.0f", total));

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnDownloadReceipt.setOnClickListener(v -> generateAndDownloadReceipt());

        binding.btnViewAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, AppointmentsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnBackToHome.setOnClickListener(v -> navigateToHome());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        navigateToHome();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void generateAndDownloadReceipt() {
        try {
            File pdf = ReceiptGenerator.generateReceipt(this,
                    appointmentId, doctorName, selectedDate, selectedTime,
                    service != null ? service : "Consultation", total, "Card");

            if (pdf != null) {
                Uri uri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", pdf);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                Toast.makeText(this, "Receipt saved to Downloads", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to generate receipt", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to generate receipt", Toast.LENGTH_SHORT).show();
        }
    }
}
