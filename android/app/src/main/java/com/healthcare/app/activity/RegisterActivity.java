package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthcare.app.databinding.ActivityRegisterBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnCreateAccount.setOnClickListener(v -> attemptRegister());

        binding.tvSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String name = getText(binding.etName);
        String email = getText(binding.etEmail);
        String phone = getText(binding.etPhone);
        String password = getText(binding.etPassword);

        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);

        if (name.isEmpty()) { binding.tilName.setError("Name is required"); return; }
        if (email.isEmpty()) { binding.tilEmail.setError("Email is required"); return; }
        if (phone.isEmpty()) { binding.tilPhone.setError("Phone is required"); return; }
        if (password.isEmpty()) { binding.tilPassword.setError("Password is required"); return; }
        if (password.length() < 6) { binding.tilPassword.setError("Password must be at least 6 characters"); return; }

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        String patientId = "PT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", user.getUid());
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("phone", phone);
                        userData.put("patientId", patientId);
                        userData.put("address", "");

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getUid())
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    setLoading(false);
                                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    setLoading(false);
                                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnCreateAccount.setEnabled(!loading);
        binding.etName.setEnabled(!loading);
        binding.etEmail.setEnabled(!loading);
        binding.etPhone.setEnabled(!loading);
        binding.etPassword.setEnabled(!loading);
    }
}
