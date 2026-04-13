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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        appointmentId = getIntent().getStringExtra("appointmentId");
        binding.tvAppointmentId.setText(appointmentId != null ? appointmentId : "");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBackToAppointments.setOnClickListener(v -> finish());

        if (appointmentId != null) {
            loadAppointment();
            performCheckIn();
        }
    }

    private void loadAppointment() {
        db.collection("appointments")
                .whereEqualTo("appointmentId", appointmentId)
                .limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Appointment appointment = doc.toObject(Appointment.class);
                        populateUI(appointment);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load appointment", Toast.LENGTH_SHORT).show());
    }

    private void performCheckIn() {
        db.collection("appointments")
                .whereEqualTo("appointmentId", appointmentId)
                .limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().update("queueStatus", "waiting", "queueNumber", 5)
                                .addOnSuccessListener(aVoid -> loadAppointment());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Check-in failed", Toast.LENGTH_SHORT).show());
    }

    private void populateUI(Appointment appointment) {
        generateQrCode(appointment.getQrCode());
        Integer queueNum = appointment.getQueueNumber();
        binding.tvQueueNumber.setText(queueNum != null ? "#" + queueNum : "#-");
        binding.tvDoctorName.setText(appointment.getDoctorName());
        binding.tvLocation.setText(appointment.getHospital());
        String schedule = (appointment.getDate() != null ? appointment.getDate() : "") + " - " + (appointment.getTime() != null ? appointment.getTime() : "");
        binding.tvSchedule.setText(schedule);
        updateQueueStatus(appointment.getQueueStatus());
    }

    private void generateQrCode(String qrData) {
        if (qrData == null || qrData.isEmpty()) return;
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bitmap = new BarcodeEncoder().createBitmap(matrix);
            binding.imgQrCode.setImageBitmap(bitmap);
        } catch (Exception e) { Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show(); }
    }

    private void updateQueueStatus(String status) {
        if (status == null) status = "waiting";
        String[] steps = {"waiting", "next", "consulting", "completed"};
        FrameLayout[] stepViews = {binding.stepWaiting, binding.stepNext, binding.stepConsulting, binding.stepCompleted};
        View[] lines = {binding.line1, binding.line2, binding.line3};
        TextView[] badges = {binding.badgeWaiting, binding.badgeNext, binding.badgeConsulting, binding.badgeCompleted};
        int currentIndex = 0;
        for (int i = 0; i < steps.length; i++) { if (steps[i].equalsIgnoreCase(status)) { currentIndex = i; break; } }
        int activeColor = ContextCompat.getColor(this, R.color.pastel_blue);
        int inactiveColor = ContextCompat.getColor(this, R.color.healthcare_gray);
        for (int i = 0; i < stepViews.length; i++) {
            GradientDrawable circle = new GradientDrawable(); circle.setShape(GradientDrawable.OVAL);
            circle.setColor(i <= currentIndex ? activeColor : inactiveColor);
            stepViews[i].getChildAt(0).setBackground(circle);
            badges[i].setVisibility(i == currentIndex ? View.VISIBLE : View.GONE);
            if (i == currentIndex) { GradientDrawable badgeBg = new GradientDrawable(); badgeBg.setShape(GradientDrawable.RECTANGLE); badgeBg.setCornerRadius(20f); badgeBg.setColor(activeColor); badges[i].setBackground(badgeBg); }
        }
        for (int i = 0; i < lines.length; i++) lines[i].setBackgroundColor(i < currentIndex ? activeColor : inactiveColor);
    }
}
