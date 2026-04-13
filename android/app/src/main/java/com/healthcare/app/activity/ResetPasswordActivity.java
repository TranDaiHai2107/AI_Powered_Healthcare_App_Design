package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.healthcare.app.databinding.ActivityResetPasswordBinding;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnResetPassword.setOnClickListener(v -> attemptReset());
        binding.tvSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptReset() {
        String newPassword = binding.etNewPassword.getText() != null
                ? binding.etNewPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null
                ? binding.etConfirmPassword.getText().toString().trim() : "";

        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        if (newPassword.isEmpty()) { binding.tilNewPassword.setError("Password is required"); return; }
        if (newPassword.length() < 6) { binding.tilNewPassword.setError("Password must be at least 6 characters"); return; }
        if (confirmPassword.isEmpty()) { binding.tilConfirmPassword.setError("Please confirm your password"); return; }
        if (!newPassword.equals(confirmPassword)) { binding.tilConfirmPassword.setError("Passwords do not match"); return; }

        setLoading(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> {
                        setLoading(false);
                        Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            setLoading(false);
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnResetPassword.setEnabled(!loading);
        binding.etNewPassword.setEnabled(!loading);
        binding.etConfirmPassword.setEnabled(!loading);
    }
}
