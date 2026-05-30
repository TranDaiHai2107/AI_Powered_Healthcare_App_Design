package com.healthcare.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.healthcare.app.R;

import java.util.HashMap;
import java.util.Map;

public class SupportActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;
    private Spinner categorySpinner;
    private EditText messageInput;
    private LinearLayout ticketList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        userId = resolveUserId();
        buildUi();
        loadTickets();
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(getColor(R.color.healthcare_gray));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(44), dp(20), dp(24));
        scrollView.addView(root);

        root.addView(title("Help & Support"));
        root.addView(body("Get help with booking, payment, check-in, insurance, and medical records."));

        LinearLayout faq = card();
        faq.addView(section("Quick Answers"));
        faq.addView(body("Booking code and QR are available after payment.\nYou can cancel or reschedule from Appointments.\nReceipts are available for completed payments.\nInsurance eligibility depends on the selected hospital."));
        root.addView(faq);

        LinearLayout contact = card();
        contact.addView(section("Contact"));
        MaterialButton call = button("Call hotline");
        call.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:19001001"))));
        MaterialButton email = outlineButton("Email support");
        email.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@healthcare.app"))));
        contact.addView(call);
        contact.addView(email);
        root.addView(contact);

        LinearLayout form = card();
        form.addView(section("Create Ticket"));
        categorySpinner = new Spinner(this);
        String[] categories = {"Booking issue", "Payment issue", "Hospital check-in", "Medical record", "Insurance", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        form.addView(categorySpinner);
        messageInput = new EditText(this);
        messageInput.setHint("Describe the issue");
        messageInput.setMinLines(4);
        messageInput.setGravity(Gravity.TOP);
        messageInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        form.addView(messageInput);
        MaterialButton submit = button("Submit ticket");
        submit.setOnClickListener(v -> submitTicket());
        form.addView(submit);
        root.addView(form);

        root.addView(section("Your Tickets"));
        ticketList = new LinearLayout(this);
        ticketList.setOrientation(LinearLayout.VERTICAL);
        root.addView(ticketList);
        setContentView(scrollView);
    }

    private void submitTicket() {
        if (userId.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = messageInput.getText() != null ? messageInput.getText().toString().trim() : "";
        if (message.length() < 10) {
            Toast.makeText(this, "Please describe the issue in more detail", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("userId", userId);
        ticket.put("category", categorySpinner.getSelectedItem().toString());
        ticket.put("message", message);
        ticket.put("status", "open");
        ticket.put("createdAt", FieldValue.serverTimestamp());

        db.collection("support_tickets").add(ticket)
                .addOnSuccessListener(doc -> {
                    messageInput.setText("");
                    Toast.makeText(this, "Ticket submitted", Toast.LENGTH_SHORT).show();
                    loadTickets();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Submit failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadTickets() {
        ticketList.removeAllViews();
        if (userId.isEmpty()) {
            ticketList.addView(body("Login required to view tickets."));
            return;
        }
        db.collection("support_tickets").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(snapshot -> {
                    ticketList.removeAllViews();
                    if (snapshot.isEmpty()) {
                        ticketList.addView(body("No tickets yet."));
                        return;
                    }
                    for (QueryDocumentSnapshot doc : snapshot) {
                        LinearLayout row = card();
                        row.addView(section(doc.getString("category") != null ? doc.getString("category") : "Ticket"));
                        row.addView(body("Status: " + (doc.getString("status") != null ? doc.getString("status") : "open") + "\n" + doc.getString("message")));
                        ticketList.addView(row);
                    }
                });
    }

    private String resolveUserId() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) return uid;
        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        return prefs.getString("userId", "");
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
        view.setText(text != null ? text : "");
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
