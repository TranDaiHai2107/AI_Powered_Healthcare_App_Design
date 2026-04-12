package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityPaymentBinding;
import com.healthcare.app.model.Doctor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private FirebaseFirestore db;
    private String doctorId;
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

        setupClickListeners();
        loadDoctor();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnApplyVoucher.setOnClickListener(v -> Toast.makeText(this, "Invalid voucher code", Toast.LENGTH_SHORT).show());
        setupPaymentMethodListeners();
        binding.btnPay.setOnClickListener(v -> {
            if (selectedPaymentMethod == null) { Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show(); return; }
            processPayment();
        });
    }

    private void setupPaymentMethodListeners() {
        binding.btnPaymentCard.setOnClickListener(v -> selectPaymentMethod(binding.btnPaymentCard, binding.checkCard, "card"));
        binding.btnPaymentWallet.setOnClickListener(v -> selectPaymentMethod(binding.btnPaymentWallet, binding.checkWallet, "wallet"));
        binding.btnPaymentBank.setOnClickListener(v -> selectPaymentMethod(binding.btnPaymentBank, binding.checkBank, "bank"));
    }

    private void selectPaymentMethod(LinearLayout btn, ImageView check, String method) {
        if (lastSelectedPayment != null) lastSelectedPayment.setBackgroundResource(R.drawable.bg_payment_unselected);
        if (lastSelectedCheck != null) lastSelectedCheck.setVisibility(View.GONE);
        btn.setBackgroundResource(R.drawable.bg_payment_selected); check.setVisibility(View.VISIBLE);
        lastSelectedPayment = btn; lastSelectedCheck = check; selectedPaymentMethod = method;
        binding.cardDetails.setVisibility("card".equals(method) ? View.VISIBLE : View.GONE);
        binding.btnPay.setEnabled(true);
    }

    private void loadDoctor() {
        if (doctorId == null) { Toast.makeText(this, "Invalid doctor", Toast.LENGTH_SHORT).show(); finish(); return; }
        db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) { currentDoctor = doc.toObject(Doctor.class); if (currentDoctor != null) populateUI(currentDoctor); }
                    else Toast.makeText(this, "Doctor not found", Toast.LENGTH_SHORT).show();
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
        binding.tvTotal.setText(String.format(Locale.US, "$%.0f", total));
        binding.btnPay.setText(String.format(Locale.US, "Pay $%.0f", total));
    }

    private void processPayment() {
        binding.btnPay.setEnabled(false); binding.btnPay.setText("Processing...");
        double total = consultationFee + SERVICE_FEE - discount;
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        Map<String, Object> payment = new HashMap<>();
        payment.put("userId", uid);
        payment.put("doctorId", doctorId);
        payment.put("amount", total);
        payment.put("paymentMethod", selectedPaymentMethod);
        payment.put("status", "completed");
        payment.put("paymentId", "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        db.collection("payments").add(payment)
                .addOnSuccessListener(docRef -> navigateToConfirmation())
                .addOnFailureListener(e -> {
                    binding.btnPay.setEnabled(true); updateTotal();
                    Toast.makeText(this, "Payment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToConfirmation() {
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra("doctorId", doctorId);
        intent.putExtra("total", consultationFee + SERVICE_FEE - discount);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent); finish();
    }
}
