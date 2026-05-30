package com.healthcare.app.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.healthcare.app.databinding.ActivityStaffScanBinding;
import com.healthcare.app.model.Appointment;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class StaffScanActivity extends AppCompatActivity {

    private ActivityStaffScanBinding binding;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    processScanResult(result.getContents());
                } else {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        binding.btnScan.setOnClickListener(v -> launchScanner());
        binding.btnScanNext.setOnClickListener(v -> {
            binding.cardResult.setVisibility(View.GONE);
            launchScanner();
        });

        if (getIntent().hasExtra("scannedContent")) {
            String content = getIntent().getStringExtra("scannedContent");
            processScanResult(content);
        }
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan the patient's appointment QR code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanLauncher.launch(options);
    }

    private void processScanResult(String content) {
        // Format: CHECKIN:<appointmentId>:<uid>
        String[] parts = content.split(":");
        if (parts.length < 2 || !parts[0].equals("CHECKIN")) {
            Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_LONG).show();
            return;
        }

        String appointmentId = parts[1];
        validateAndUpdateCheckIn(appointmentId);
    }

    private void validateAndUpdateCheckIn(String appointmentId) {
        db.collection("appointments")
                .whereEqualTo("appointmentId", appointmentId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Appointment not found: " + appointmentId, Toast.LENGTH_LONG).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Appointment appointment = doc.toObject(Appointment.class);
                        if (!"not_checked_in".equals(appointment.getQueueStatus())) {
                            Toast.makeText(this, "Already checked in!", Toast.LENGTH_SHORT).show();
                            showResult(appointment);
                            return;
                        }

                        calculateAndPerformCheckIn(doc, appointment);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void calculateAndPerformCheckIn(QueryDocumentSnapshot doc, Appointment appointment) {
        String today = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());
        
        // Use today from the appointment if possible to ensure we count for the correct day's queue
        String appointmentDate = appointment.getDate() != null ? appointment.getDate() : today;

        db.collection("appointments")
                .whereEqualTo("date", appointmentDate)
                .whereIn("queueStatus", Arrays.asList("waiting", "next", "consulting", "completed"))
                .get()
                .addOnSuccessListener(snap -> {
                    int nextQueueNumber = snap.size() + 1;
                    performUpdate(doc, appointment, nextQueueNumber);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error calculating queue", Toast.LENGTH_SHORT).show());
    }

    private void performUpdate(QueryDocumentSnapshot doc, Appointment appointment, int queueNumber) {
        doc.getReference().update(
                "queueStatus", "waiting",
                "queueNumber", queueNumber,
                "checkInTime", FieldValue.serverTimestamp()
        ).addOnSuccessListener(aVoid -> {
            appointment.setQueueStatus("waiting");
            appointment.setQueueNumber(queueNumber);
            showResult(appointment);
            Toast.makeText(this, "Check-in successful!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showResult(Appointment appointment) {
        binding.cardResult.setVisibility(View.VISIBLE);
        binding.tvPatientName.setText("ID: " + appointment.getAppointmentId()); // Using ID as proxy for name in this demo
        binding.tvDoctorName.setText("Doctor: " + appointment.getDoctorName());
        binding.tvQueueNumber.setText("#" + appointment.getQueueNumber());
    }
}
