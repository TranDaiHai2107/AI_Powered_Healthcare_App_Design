package com.healthcare.app.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityCheckinBinding;
import com.healthcare.app.model.Appointment;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class CheckInActivity extends AppCompatActivity {

    private ActivityCheckinBinding binding;
    private FirebaseFirestore db;
    private String appointmentId;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        appointmentId = getIntent().getStringExtra("appointmentId");

        binding.tvAppointmentId.setText(appointmentId != null ? "ID: " + appointmentId : "");
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBackToAppointments.setOnClickListener(v -> finish());

        if (appointmentId == null) {
            Toast.makeText(this, "Invalid appointment", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate QR at full opacity
        generateQrCode("CHECKIN:" + appointmentId);

        // Load appointment data then start listener
        loadAppointment();
    }

    private void loadAppointment() {
        db.collection("appointments")
                .whereEqualTo("appointmentId", appointmentId)
                .limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Appointment appointment = doc.toObject(Appointment.class);
                        populateDetails(appointment);

                        String queueStatus = appointment.getQueueStatus();
                        if (queueStatus != null && !queueStatus.equals("not_checked_in")) {
                            // Already checked in — show success directly
                            showSuccessState(appointment.getQueueNumber());
                        } else {
                            // Not checked in yet — show waiting instruction, start listener for staff-side updates
                            showWaitingState();
                            startRealtimeListener();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load appointment", Toast.LENGTH_SHORT).show());
    }

    // Show waiting state: QR visible, instruction text visible, no scan button
    private void showWaitingState() {
        binding.imgQrCode.setAlpha(1.0f);
        binding.btnScanToCheckin.setVisibility(View.GONE);
        binding.tvCheckinSuccess.setVisibility(View.GONE);
        binding.tvWaitingInstruction.setVisibility(View.VISIBLE);
    }

    // Show the success state: success banner visible, QR dimmed
    private void showSuccessState(Integer queueNumber) {
        // Remove realtime listener — no longer needed
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
        binding.imgQrCode.setAlpha(0.4f);
        binding.btnScanToCheckin.setVisibility(View.GONE);
        binding.tvWaitingInstruction.setVisibility(View.GONE);
        binding.tvCheckinSuccess.setVisibility(View.VISIBLE);
        if (queueNumber != null) {
            binding.tvQueueNumber.setText("#" + queueNumber);
        }
    }

    // Listener for staff-side updates
    private void startRealtimeListener() {
        listenerRegistration = db.collection("appointments")
                .whereEqualTo("appointmentId", appointmentId)
                .limit(1)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) return;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Appointment appointment = doc.toObject(Appointment.class);
                        String queueStatus = appointment.getQueueStatus();
                        updateQueueTracker(queueStatus);
                        if (queueStatus != null && !queueStatus.equals("not_checked_in")) {
                            showSuccessState(appointment.getQueueNumber());
                        }
                    }
                });
    }

    private void populateDetails(Appointment appointment) {
        String qrData = appointment.getQrCode();
        if (qrData == null || qrData.isEmpty()) qrData = "CHECKIN:" + appointmentId;
        generateQrCode(qrData);

        Integer queueNum = appointment.getQueueNumber();
        binding.tvQueueNumber.setText(queueNum != null ? "#" + queueNum : "#—");
        binding.tvDoctorName.setText(appointment.getDoctorName() != null ? appointment.getDoctorName() : "—");
        binding.tvLocation.setText(appointment.getHospital() != null ? appointment.getHospital() : "—");
        String schedule = (appointment.getDate() != null ? appointment.getDate() : "")
                + " — " + (appointment.getTime() != null ? appointment.getTime() : "");
        binding.tvSchedule.setText(schedule);
        updateQueueTracker(appointment.getQueueStatus());
    }

    private void generateQrCode(String qrData) {
        if (qrData == null || qrData.isEmpty()) return;
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bitmap = new BarcodeEncoder().createBitmap(matrix);
            binding.imgQrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateQueueTracker(String status) {
        if (status == null) status = "not_checked_in";
        String[] steps = {"waiting", "next", "consulting", "completed"};
        FrameLayout[] stepViews = {binding.stepWaiting, binding.stepNext, binding.stepConsulting, binding.stepCompleted};
        View[] lines = {binding.line1, binding.line2, binding.line3};
        TextView[] badges = {binding.badgeWaiting, binding.badgeNext, binding.badgeConsulting, binding.badgeCompleted};

        int currentIndex = -1;
        for (int i = 0; i < steps.length; i++) {
            if (steps[i].equalsIgnoreCase(status)) { currentIndex = i; break; }
        }

        int activeColor = ContextCompat.getColor(this, R.color.pastel_blue);
        int inactiveColor = ContextCompat.getColor(this, R.color.healthcare_gray);

        for (int i = 0; i < stepViews.length; i++) {
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(i <= currentIndex ? activeColor : inactiveColor);
            stepViews[i].getChildAt(0).setBackground(circle);
            badges[i].setVisibility(i == currentIndex ? View.VISIBLE : View.GONE);
            if (i == currentIndex) {
                GradientDrawable badgeBg = new GradientDrawable();
                badgeBg.setShape(GradientDrawable.RECTANGLE);
                badgeBg.setCornerRadius(20f);
                badgeBg.setColor(activeColor);
                badges[i].setBackground(badgeBg);
            }
        }
        for (int i = 0; i < lines.length; i++) {
            lines[i].setBackgroundColor(i < currentIndex ? activeColor : inactiveColor);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}
