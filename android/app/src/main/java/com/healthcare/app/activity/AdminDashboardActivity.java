package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthcare.app.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout metricsContainer;
    private EditText doctorIdInput;
    private EditText feeInput;
    private EditText slotsInput;
    private EditText voucherCodeInput;
    private EditText voucherValueInput;
    private EditText bannerTitleInput;
    private EditText bannerMessageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        buildUi();
        loadMetrics();
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(getColor(R.color.healthcare_gray));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(44), dp(20), dp(24));
        scrollView.addView(root);

        root.addView(title("Partner Admin"));
        root.addView(body("Manage queue check-in, doctor pricing, available slots, vouchers, banners, and operations reports."));

        MaterialButton scan = button("Open QR Check-in Scanner");
        scan.setOnClickListener(v -> startActivity(new Intent(this, StaffScanActivity.class)));
        root.addView(scan);

        root.addView(section("Reports"));
        metricsContainer = new LinearLayout(this);
        metricsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(metricsContainer);

        LinearLayout doctorCard = card();
        doctorCard.addView(section("Doctor Schedule & Price"));
        doctorIdInput = input("Doctor document ID");
        feeInput = input("Consultation fee");
        feeInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        slotsInput = input("Slots, comma separated: 09:00 AM,10:00 AM");
        doctorCard.addView(doctorIdInput);
        doctorCard.addView(feeInput);
        doctorCard.addView(slotsInput);
        MaterialButton updateDoctor = button("Update doctor");
        updateDoctor.setOnClickListener(v -> updateDoctor());
        doctorCard.addView(updateDoctor);
        root.addView(doctorCard);

        LinearLayout voucherCard = card();
        voucherCard.addView(section("Voucher"));
        voucherCodeInput = input("Code, e.g. HEALTH10");
        voucherValueInput = input("Discount value");
        voucherValueInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        voucherCard.addView(voucherCodeInput);
        voucherCard.addView(voucherValueInput);
        MaterialButton saveVoucher = button("Save voucher");
        saveVoucher.setOnClickListener(v -> saveVoucher());
        voucherCard.addView(saveVoucher);
        root.addView(voucherCard);

        LinearLayout bannerCard = card();
        bannerCard.addView(section("Content Banner"));
        bannerTitleInput = input("Banner title");
        bannerMessageInput = input("Banner message");
        bannerCard.addView(bannerTitleInput);
        bannerCard.addView(bannerMessageInput);
        MaterialButton saveBanner = button("Publish banner");
        saveBanner.setOnClickListener(v -> saveBanner());
        bannerCard.addView(saveBanner);
        root.addView(bannerCard);

        setContentView(scrollView);
    }

    private void loadMetrics() {
        metricsContainer.removeAllViews();
        addCountMetric("Total bookings", "appointments", null, null);
        addCountMetric("Upcoming bookings", "appointments", "status", "upcoming");
        addCountMetric("Cancelled bookings", "appointments", "status", "cancelled");
        addCountMetric("Paid transactions", "payments", "status", "completed");
        addCountMetric("Support tickets", "support_tickets", null, null);
        addCountMetric("Reviews", "reviews", null, null);
    }

    private void addCountMetric(String label, String collection, String field, String value) {
        com.google.firebase.firestore.Query query = db.collection(collection);
        if (field != null) query = query.whereEqualTo(field, value);
        query.get().addOnSuccessListener(snapshot -> metricsContainer.addView(metric(label, snapshot.size())))
                .addOnFailureListener(e -> metricsContainer.addView(metric(label, 0)));
    }

    private void updateDoctor() {
        String doctorId = text(doctorIdInput);
        if (doctorId.isEmpty()) {
            Toast.makeText(this, "Doctor ID is required", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> data = new HashMap<>();
        if (!text(feeInput).isEmpty()) data.put("consultationFee", Double.parseDouble(text(feeInput)));
        if (!text(slotsInput).isEmpty()) data.put("availableSlots", text(slotsInput));
        data.put("updatedAt", FieldValue.serverTimestamp());
        db.collection("doctors").document(doctorId).update(data)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Doctor updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveVoucher() {
        String code = text(voucherCodeInput).toUpperCase(Locale.US);
        String value = text(voucherValueInput);
        if (code.isEmpty() || value.isEmpty()) {
            Toast.makeText(this, "Code and value are required", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("discountValue", Double.parseDouble(value));
        data.put("active", true);
        data.put("createdAt", FieldValue.serverTimestamp());
        db.collection("vouchers").document(code).set(data)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Voucher saved", Toast.LENGTH_SHORT).show());
    }

    private void saveBanner() {
        String title = text(bannerTitleInput);
        String message = text(bannerMessageInput);
        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Title and message are required", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);
        data.put("active", true);
        data.put("createdAt", FieldValue.serverTimestamp());
        db.collection("content_banners").add(data)
                .addOnSuccessListener(doc -> Toast.makeText(this, "Banner published", Toast.LENGTH_SHORT).show());
    }

    private String text(EditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private TextView metric(String label, int value) {
        TextView view = body(label + ": " + value);
        view.setBackgroundColor(getColor(R.color.white));
        view.setPadding(dp(16), dp(12), dp(16), dp(12));
        return view;
    }

    private TextView title(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(getColor(R.color.healthcare_dark));
        view.setTextSize(24);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private TextView section(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(getColor(R.color.healthcare_muted));
        view.setTextSize(12);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setPadding(0, dp(10), 0, dp(6));
        return view;
    }

    private TextView body(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(getColor(R.color.healthcare_text));
        view.setTextSize(14);
        view.setPadding(0, dp(8), 0, dp(8));
        return view;
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setTextSize(14);
        input.setPadding(dp(12), 0, dp(12), 0);
        return input;
    }

    private MaterialButton button(String text) {
        MaterialButton button = new MaterialButton(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setCornerRadius(dp(14));
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.pastel_mint)));
        button.setTextColor(getColor(R.color.healthcare_dark));
        return button;
    }

    private LinearLayout card() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(14), dp(16), dp(14));
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(getColor(R.color.white));
        bg.setCornerRadius(dp(16));
        layout.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(12), 0, 0);
        layout.setLayoutParams(params);
        return layout;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
