package com.healthcare.app.activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.healthcare.app.R;

import java.util.HashMap;
import java.util.Map;

public class AppSettingsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String userId;
    private LinearLayout historyList;
    private SwitchCompat pushSwitch;
    private SwitchCompat smsSwitch;
    private SwitchCompat emailSwitch;
    private SwitchCompat medicineSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = resolveUserId();
        buildUi();
        saveCurrentDevice();
        loadSettings();
        loadLoginHistory();
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(getColor(R.color.healthcare_gray));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(44), dp(20), dp(24));
        scrollView.addView(root);

        root.addView(title("App Settings"));
        root.addView(body("Manage notifications, password security, privacy, and trusted devices."));

        LinearLayout notificationCard = card();
        notificationCard.addView(section("Notifications"));
        pushSwitch = settingSwitch("Push notification");
        smsSwitch = settingSwitch("SMS reminders");
        emailSwitch = settingSwitch("Email updates");
        medicineSwitch = settingSwitch("Medicine and follow-up reminders");
        notificationCard.addView(pushSwitch);
        notificationCard.addView(smsSwitch);
        notificationCard.addView(emailSwitch);
        notificationCard.addView(medicineSwitch);
        root.addView(notificationCard);

        LinearLayout securityCard = card();
        securityCard.addView(section("Security"));
        MaterialButton resetPassword = button("Send password reset email");
        resetPassword.setOnClickListener(v -> sendPasswordReset());
        MaterialButton privacy = outlineButton("Privacy Policy");
        privacy.setOnClickListener(v -> showInfo("Privacy Policy", "Sensitive health data, insurance documents, and identity data are stored for appointment and care workflows only. Access should be limited by user, hospital, and admin roles. Production builds should enforce Firebase Security Rules, encryption at rest, and audit logs."));
        MaterialButton terms = outlineButton("Terms & Conditions");
        terms.setOnClickListener(v -> showInfo("Terms & Conditions", "Bookings, cancellations, deposits, refunds, insurance estimates, and hospital queue status depend on partner policies. Medical records are shown only when the hospital integration provides them."));
        securityCard.addView(resetPassword);
        securityCard.addView(privacy);
        securityCard.addView(terms);
        root.addView(securityCard);

        root.addView(section("Login History & Devices"));
        historyList = new LinearLayout(this);
        historyList.setOrientation(LinearLayout.VERTICAL);
        root.addView(historyList);
        setContentView(scrollView);
    }

    private void loadSettings() {
        if (userId.isEmpty()) return;
        db.collection("user_settings").document(userId).get()
                .addOnSuccessListener(doc -> {
                    boolean push = doc.getBoolean("pushNotifications") == null || Boolean.TRUE.equals(doc.getBoolean("pushNotifications"));
                    boolean sms = Boolean.TRUE.equals(doc.getBoolean("smsNotifications"));
                    boolean email = doc.getBoolean("emailNotifications") == null || Boolean.TRUE.equals(doc.getBoolean("emailNotifications"));
                    boolean medicine = Boolean.TRUE.equals(doc.getBoolean("medicationReminders"));
                    pushSwitch.setChecked(push);
                    smsSwitch.setChecked(sms);
                    emailSwitch.setChecked(email);
                    medicineSwitch.setChecked(medicine);
                    attachSwitchListeners();
                })
                .addOnFailureListener(e -> attachSwitchListeners());
    }

    private void attachSwitchListeners() {
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> saveSettings();
        pushSwitch.setOnCheckedChangeListener(listener);
        smsSwitch.setOnCheckedChangeListener(listener);
        emailSwitch.setOnCheckedChangeListener(listener);
        medicineSwitch.setOnCheckedChangeListener(listener);
    }

    private void saveSettings() {
        if (userId.isEmpty()) return;
        Map<String, Object> data = new HashMap<>();
        data.put("pushNotifications", pushSwitch.isChecked());
        data.put("smsNotifications", smsSwitch.isChecked());
        data.put("emailNotifications", emailSwitch.isChecked());
        data.put("medicationReminders", medicineSwitch.isChecked());
        data.put("updatedAt", FieldValue.serverTimestamp());
        db.collection("user_settings").document(userId).set(data);
    }

    private void sendPasswordReset() {
        String email = firebaseUser != null ? firebaseUser.getEmail() : null;
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "No email is attached to this account", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Unable to send email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveCurrentDevice() {
        if (userId.isEmpty()) return;
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Map<String, Object> data = new HashMap<>();
        data.put("deviceId", androidId);
        data.put("deviceName", Build.MANUFACTURER + " " + Build.MODEL);
        data.put("osVersion", "Android " + Build.VERSION.RELEASE);
        data.put("lastSeenAt", FieldValue.serverTimestamp());
        db.collection("users").document(userId).collection("login_history").document(androidId).set(data);
    }

    private void loadLoginHistory() {
        historyList.removeAllViews();
        if (userId.isEmpty()) {
            historyList.addView(body("Login required to view devices."));
            return;
        }
        db.collection("users").document(userId).collection("login_history").get()
                .addOnSuccessListener(snapshot -> {
                    historyList.removeAllViews();
                    if (snapshot.isEmpty()) {
                        historyList.addView(body("No device history yet."));
                        return;
                    }
                    for (QueryDocumentSnapshot doc : snapshot) {
                        LinearLayout row = card();
                        row.addView(section(doc.getString("deviceName") != null ? doc.getString("deviceName") : "Device"));
                        row.addView(body(doc.getString("osVersion") != null ? doc.getString("osVersion") : "Android"));
                        historyList.addView(row);
                    }
                });
    }

    private void showInfo(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private String resolveUserId() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) return uid;
        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        return prefs.getString("userId", "");
    }

    private SwitchCompat settingSwitch(String text) {
        SwitchCompat view = new SwitchCompat(this);
        view.setText(text);
        view.setTextColor(getColor(R.color.healthcare_text));
        view.setTextSize(14);
        view.setPadding(0, dp(8), 0, dp(8));
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
        view.setLineSpacing(2, 1.1f);
        view.setPadding(0, dp(8), 0, dp(8));
        return view;
    }

    private MaterialButton button(String text) {
        MaterialButton button = new MaterialButton(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setCornerRadius(dp(14));
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.pastel_blue)));
        button.setTextColor(getColor(R.color.healthcare_dark));
        return button;
    }

    private MaterialButton outlineButton(String text) {
        MaterialButton button = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText(text);
        button.setAllCaps(false);
        button.setCornerRadius(dp(14));
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
