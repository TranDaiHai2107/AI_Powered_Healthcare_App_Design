package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityPaymentBinding;
import com.healthcare.app.model.Doctor;
import com.healthcare.app.util.ReminderScheduler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private FirebaseFirestore db;
    private String doctorId;
    private String selectedDate;
    private String selectedTime;
    private String selectedService;
    private String doctorName;
    private String doctorSpecialization;
    private String doctorImage;
    private String hospitalName;
    private String patientName;
    private String familyMemberId;
    private String symptoms;
    private Doctor currentDoctor;

    private static final double SERVICE_FEE = 5.0;
    private double consultationFee = 0;
    private double discount = 0;
    private String selectedPaymentMethod = null;

    private LinearLayout lastSelectedPayment = null;
    private ImageView lastSelectedCheck = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        doctorId = getIntent().getStringExtra("doctorId");
        selectedDate = getIntent().getStringExtra("selectedDate");
        selectedTime = getIntent().getStringExtra("selectedTime");
        selectedService = getIntent().getStringExtra("selectedService");
        doctorName = getIntent().getStringExtra("doctorName");
        doctorSpecialization = getIntent().getStringExtra("doctorSpecialization");
        doctorImage = getIntent().getStringExtra("doctorImage");
        hospitalName = getIntent().getStringExtra("hospitalName");
        patientName = getIntent().getStringExtra("patientName");
        familyMemberId = getIntent().getStringExtra("familyMemberId");
        symptoms = getIntent().getStringExtra("symptoms");

        setupClickListeners();
        loadDoctor();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnApplyVoucher.setOnClickListener(v -> applyVoucher());
        setupPaymentMethodListeners();
        binding.btnPay.setOnClickListener(v -> {
            if (selectedPaymentMethod == null) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                return;
            }
            processPayment();
        });
    }

    private void applyVoucher() {
        String code = binding.etVoucherCode.getText() != null
                ? binding.etVoucherCode.getText().toString().trim() : "";
        if (code.equalsIgnoreCase("HEALTH10")) {
            discount = consultationFee * 0.10;
            updateTotal();
            Toast.makeText(this, "Voucher applied! 10% discount", Toast.LENGTH_SHORT).show();
        } else if (code.equalsIgnoreCase("SAVE20")) {
            discount = 20.0;
            updateTotal();
            Toast.makeText(this, "Voucher applied! $20 off", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Invalid voucher code", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPaymentMethodListeners() {
        binding.btnPaymentCard.setOnClickListener(v -> selectPaymentMethod(binding.btnPaymentCard, binding.checkCard, "card"));
        binding.btnPaymentWallet.setOnClickListener(v -> selectPaymentMethod(binding.btnPaymentWallet, binding.checkWallet, "wallet"));
        binding.btnPaymentBank.setOnClickListener(v -> selectPaymentMethod(binding.btnPaymentBank, binding.checkBank, "bank"));
    }

    private void selectPaymentMethod(LinearLayout btn, ImageView check, String method) {
        if (lastSelectedPayment != null) lastSelectedPayment.setBackgroundResource(R.drawable.bg_payment_unselected);
        if (lastSelectedCheck != null) lastSelectedCheck.setVisibility(View.GONE);
        btn.setBackgroundResource(R.drawable.bg_payment_selected);
        check.setVisibility(View.VISIBLE);
        lastSelectedPayment = btn;
        lastSelectedCheck = check;
        selectedPaymentMethod = method;
        binding.cardDetails.setVisibility("card".equals(method) ? View.VISIBLE : View.GONE);
        binding.btnPay.setEnabled(true);
    }

    private void loadDoctor() {
        if (doctorId == null) {
            Toast.makeText(this, "Invalid doctor", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentDoctor = doc.toObject(Doctor.class);
                        if (currentDoctor != null) {
                            if (doctorName == null) doctorName = currentDoctor.getName();
                            if (doctorSpecialization == null) doctorSpecialization = currentDoctor.getSpecialization();
                            if (doctorImage == null) doctorImage = currentDoctor.getImage();
                            if (hospitalName == null) hospitalName = currentDoctor.getHospitalName();
                            populateUI(currentDoctor);
                        }
                    } else {
                        Toast.makeText(this, "Doctor not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load doctor", Toast.LENGTH_SHORT).show());
    }

    private void populateUI(Doctor doctor) {
        consultationFee = doctor.getConsultationFee() != null ? doctor.getConsultationFee() : 0;
        binding.tvConsultationFee.setText(String.format(Locale.US, "$%.0f", consultationFee));
        binding.tvServiceFee.setText(String.format(Locale.US, "$%.0f", SERVICE_FEE));
        binding.tvDiscount.setText(String.format(Locale.US, "-$%.0f", discount));
        updateTotal();
    }

    private void updateTotal() {
        double total = consultationFee + SERVICE_FEE - discount;
        if (total < 0) total = 0;
        binding.tvDiscount.setText(String.format(Locale.US, "-$%.0f", discount));
        binding.tvTotal.setText(String.format(Locale.US, "$%.0f", total));
        binding.btnPay.setText(String.format(Locale.US, "Pay $%.0f", total));
    }

    private void processPayment() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedService == null || selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Booking information is incomplete", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnPay.setEnabled(false);
        binding.btnPay.setText("Processing...");

        double total = Math.max(0, consultationFee + SERVICE_FEE - discount);
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String appointmentId = "APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String qrCode = "CHECKIN:" + appointmentId + ":" + uid;

        Map<String, Object> payment = new HashMap<>();
        payment.put("paymentId", paymentId);
        payment.put("userId", uid);
        payment.put("doctorId", doctorId);
        payment.put("appointmentId", appointmentId);
        payment.put("amount", total);
        payment.put("paymentMethod", selectedPaymentMethod);
        payment.put("status", "completed");
        payment.put("createdAt", FieldValue.serverTimestamp());

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("appointmentId", appointmentId);
        appointment.put("userId", uid);
        appointment.put("doctorId", doctorId);
        appointment.put("doctorName", doctorName);
        appointment.put("doctorSpecialization", doctorSpecialization);
        appointment.put("doctorImage", doctorImage);
        appointment.put("hospital", hospitalName);
        appointment.put("date", selectedDate);
        appointment.put("time", selectedTime);
        appointment.put("status", "upcoming");
        appointment.put("type", selectedService);
        appointment.put("paymentId", paymentId);
        appointment.put("patientName", patientName);
        appointment.put("familyMemberId", familyMemberId);
        appointment.put("symptoms", symptoms);
        appointment.put("qrCode", qrCode);
        appointment.put("queueNumber", null);
        appointment.put("queueStatus", "not_checked_in");
        appointment.put("createdAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        DocumentReference paymentRef = db.collection("payments").document(paymentId);
        DocumentReference appointmentRef = db.collection("appointments").document(appointmentId);
        batch.set(paymentRef, payment);
        batch.set(appointmentRef, appointment);

        batch.commit()
                .addOnSuccessListener(unused -> {
                    ReminderScheduler.scheduleReminder(this, appointmentId, selectedDate, selectedTime, doctorName);
                    
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("userId", uid);
                    notif.put("title", "Booking Confirmed");
                    notif.put("message", "Appointment with " + doctorName + " on " + selectedDate);
                    notif.put("type", "booking_confirmed");
                    notif.put("isRead", false);
                    notif.put("appointmentId", appointmentId);
                    notif.put("createdAt", FieldValue.serverTimestamp());
                    db.collection("notifications").add(notif);
                    
                    navigateToConfirmation(appointmentId, paymentId, total);
                })
                .addOnFailureListener(e -> {
                    binding.btnPay.setEnabled(true);
                    updateTotal();
                    Toast.makeText(this, "Payment failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToConfirmation(String appointmentId, String paymentId, double total) {
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra("appointmentId", appointmentId);
        intent.putExtra("paymentId", paymentId);
        intent.putExtra("doctorId", doctorId);
        intent.putExtra("doctorName", doctorName);
        intent.putExtra("date", selectedDate);
        intent.putExtra("time", selectedTime);
        intent.putExtra("service", selectedService);
        intent.putExtra("total", total);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
