package com.healthcare.app.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.Locale;
import java.util.Map;

public class PaymentMethodsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;
    private LinearLayout listContainer;
    private Spinner typeSpinner;
    private EditText labelInput;
    private EditText detailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        userId = resolveUserId();
        buildUi();
        loadPaymentMethods();
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(getColor(R.color.healthcare_gray));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(44), dp(20), dp(24));
        scrollView.addView(root);

        ImageView btnBack = new ImageView(this);
        btnBack.setImageResource(android.R.drawable.ic_menu_revert);
        btnBack.setImageTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.healthcare_dark)));
        btnBack.setBackground(getDrawable(R.drawable.bg_rounded_card));
        btnBack.setPadding(dp(8), dp(8), dp(8), dp(8));
        btnBack.setClipToOutline(true);
        btnBack.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        backParams.setMargins(0, 0, 0, dp(16));
        btnBack.setLayoutParams(backParams);
        root.addView(btnBack);

        TextView title = title("Payment Methods");
        root.addView(title);
        root.addView(body("Save cards, wallets, and bank transfer references for faster checkout."));

        LinearLayout form = card();
        typeSpinner = new Spinner(this);
        String[] types = {"Card", "E-wallet", "Bank transfer", "Domestic ATM"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        form.addView(label("Type"));
        form.addView(typeSpinner);

        labelInput = input("Label, e.g. Visa personal");
        form.addView(label("Name"));
        form.addView(labelInput);

        detailInput = input("Last 4 digits, wallet phone, or bank account");
        detailInput.setInputType(InputType.TYPE_CLASS_TEXT);
        form.addView(label("Reference"));
        form.addView(detailInput);

        MaterialButton saveButton = button("Save method");
        saveButton.setOnClickListener(v -> savePaymentMethod());
        form.addView(saveButton);
        root.addView(form);

        TextView sectionTitle = section("Saved Methods");
        root.addView(sectionTitle);
        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(listContainer);

        setContentView(scrollView);
    }

    private void savePaymentMethod() {
        if (userId.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String label = value(labelInput);
        String detail = value(detailInput);
        if (label.isEmpty() || detail.isEmpty()) {
            Toast.makeText(this, "Please enter method name and reference", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("type", typeSpinner.getSelectedItem().toString());
        data.put("label", label);
        data.put("reference", mask(detail));
        data.put("isDefault", false);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("payment_methods").add(data)
                .addOnSuccessListener(doc -> {
                    labelInput.setText("");
                    detailInput.setText("");
                    Toast.makeText(this, "Payment method saved", Toast.LENGTH_SHORT).show();
                    loadPaymentMethods();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadPaymentMethods() {
        listContainer.removeAllViews();
        if (userId.isEmpty()) {
            listContainer.addView(empty("Login required to view saved methods."));
            return;
        }

        db.collection("payment_methods")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    listContainer.removeAllViews();
                    if (snapshot.isEmpty()) {
                        listContainer.addView(empty("No payment methods saved yet."));
                        return;
                    }
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String type = doc.getString("type") != null ? doc.getString("type") : "Method";
                        String name = doc.getString("label") != null ? doc.getString("label") : "Payment method";
                        String ref = doc.getString("reference") != null ? doc.getString("reference") : "";
                        listContainer.addView(methodRow(type, name, ref, doc.getId()));
                    }
                })
                .addOnFailureListener(e -> listContainer.addView(empty("Unable to load methods.")));
    }

    private LinearLayout methodRow(String type, String name, String ref, String docId) {
        LinearLayout row = card();
        row.addView(section(type));
        row.addView(body(name + "\n" + ref));
        MaterialButton delete = outlineButton("Remove");
        delete.setOnClickListener(v -> db.collection("payment_methods").document(docId).delete()
                .addOnSuccessListener(unused -> loadPaymentMethods()));
        row.addView(delete);
        return row;
    }

    private String resolveUserId() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) return uid;
        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        return prefs.getString("userId", "");
    }

    private String mask(String raw) {
        String compact = raw.replaceAll("\\s+", "");
        if (compact.length() <= 4) return compact;
        return "**** " + compact.substring(compact.length() - 4);
    }

    private String value(EditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
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
        view.setText(text.toUpperCase(Locale.US));
        view.setTextColor(getColor(R.color.healthcare_muted));
        view.setTextSize(12);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setPadding(0, dp(12), 0, dp(6));
        return view;
    }

    private TextView label(String text) {
        TextView view = section(text);
        view.setPadding(0, dp(10), 0, dp(4));
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

    private TextView empty(String text) {
        TextView view = body(text);
        view.setGravity(Gravity.CENTER);
        view.setPadding(0, dp(24), 0, dp(24));
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
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.pastel_blue)));
        button.setTextColor(getColor(R.color.healthcare_dark));
        return button;
    }

    private MaterialButton outlineButton(String text) {
        MaterialButton button = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText(text);
        button.setAllCaps(false);
        button.setCornerRadius(dp(14));
        button.setTextColor(getColor(R.color.red_700));
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
