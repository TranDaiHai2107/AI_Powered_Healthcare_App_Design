package com.healthcare.app.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthcare.app.R;

import java.util.HashMap;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;
    private String appointmentId;
    private String doctorId;
    private String doctorName;
    private RatingBar ratingBar;
    private EditText commentInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        userId = resolveUserId();
        appointmentId = getIntent().getStringExtra("appointmentId");
        doctorId = getIntent().getStringExtra("doctorId");
        doctorName = getIntent().getStringExtra("doctorName");
        buildUi();
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(getColor(R.color.healthcare_gray));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(44), dp(20), dp(24));
        scrollView.addView(root);

        root.addView(title("Rate Your Visit"));
        root.addView(body(doctorName != null ? doctorName : "Completed appointment"));

        LinearLayout card = card();
        card.addView(section("Rating"));
        ratingBar = new RatingBar(this, null, android.R.attr.ratingBarStyle);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);
        ratingBar.setRating(5);
        card.addView(ratingBar);

        commentInput = new EditText(this);
        commentInput.setHint("Share your experience");
        commentInput.setMinLines(4);
        commentInput.setGravity(Gravity.TOP);
        commentInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        card.addView(commentInput);

        MaterialButton submit = button("Submit review");
        submit.setOnClickListener(v -> submitReview());
        card.addView(submit);
        root.addView(card);
        setContentView(scrollView);
    }

    private void submitReview() {
        if (userId.isEmpty() || appointmentId == null) {
            Toast.makeText(this, "Missing review information", Toast.LENGTH_SHORT).show();
            return;
        }
        String comment = commentInput.getText() != null ? commentInput.getText().toString().trim() : "";
        Map<String, Object> review = new HashMap<>();
        review.put("userId", userId);
        review.put("appointmentId", appointmentId);
        review.put("doctorId", doctorId);
        review.put("doctorName", doctorName);
        review.put("rating", ratingBar.getRating());
        review.put("comment", comment);
        review.put("createdAt", FieldValue.serverTimestamp());

        db.collection("reviews").document(appointmentId).set(review)
                .addOnSuccessListener(unused -> db.collection("appointments").document(appointmentId)
                        .update("reviewed", true)
                        .addOnSuccessListener(done -> {
                            Toast.makeText(this, "Thank you for your review", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> finish()))
                .addOnFailureListener(e -> Toast.makeText(this, "Submit failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        view.setText(text);
        view.setTextColor(getColor(R.color.healthcare_text));
        view.setTextSize(14);
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

    private LinearLayout card() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(14), dp(16), dp(14));
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(getColor(R.color.white));
        bg.setCornerRadius(dp(16));
        layout.setBackground(bg);
        return layout;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
