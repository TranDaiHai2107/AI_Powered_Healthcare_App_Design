package com.healthcare.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityProfileBinding;
import com.healthcare.app.model.User;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseFirestore db;
    private String userId;
    private User currentUser;
    private ImageView dialogAvatarImage;

    private final ActivityResultLauncher<String> getContentLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    decodeQrFromUri(uri);
                }
            });

    private final ActivityResultLauncher<String> avatarLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadAvatar(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");
        if (userId.isEmpty() && FirebaseAuth.getInstance().getUid() != null) {
            userId = FirebaseAuth.getInstance().getUid();
        }

        styleAvatar();
        setupClickListeners();
        setupBottomNav();
        loadProfile();
    }

    private void styleAvatar() {
        GradientDrawable avatarBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{
                        ContextCompat.getColor(this, R.color.pastel_blue),
                        ContextCompat.getColor(this, R.color.pastel_lavender)
                });
        avatarBg.setShape(GradientDrawable.OVAL);
        binding.viewAvatar.setBackground(avatarBg);
    }

    private void setupClickListeners() {
        // Personal Information → Edit profile dialog
        binding.menuPersonalInfo.setOnClickListener(v -> showEditProfileDialog());
        binding.layoutAvatar.setOnClickListener(v -> avatarLauncher.launch("image/*"));

        // Family Members
        binding.menuFamilyMembers.setOnClickListener(v ->
                startActivity(new Intent(this, FamilyMembersActivity.class)));

        // Insurance → Navigate to InsuranceActivity
        binding.menuInsurance.setOnClickListener(v ->
                startActivity(new Intent(this, InsuranceActivity.class)));

        // Payment Methods
        binding.menuPaymentMethods.setOnClickListener(v ->
                startActivity(new Intent(this, PaymentMethodsActivity.class)));

        // App Settings
        binding.menuAppSettings.setOnClickListener(v ->
                startActivity(new Intent(this, AppSettingsActivity.class)));

        // Help Center
        binding.menuHelpCenter.setOnClickListener(v ->
                startActivity(new Intent(this, SupportActivity.class)));

        // Contact Support
        binding.menuContactSupport.setOnClickListener(v ->
                startActivity(new Intent(this, SupportActivity.class)));

        binding.menuAdminDashboard.setOnClickListener(v ->
                startActivity(new Intent(this, AdminDashboardActivity.class)));

        // Staff Portal → Navigate to StaffScanActivity
        binding.menuStaffPortal.setOnClickListener(v ->
                startActivity(new Intent(this, StaffScanActivity.class)));

        // Upload QR Image
        binding.menuStaffUploadQr.setOnClickListener(v -> getContentLauncher.launch("image/*"));

        // Logout
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    private void decodeQrFromUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(binaryBitmap);
            if (result != null && result.getText() != null) {
                // Pass the decoded text to StaffScanActivity
                Intent intent = new Intent(this, StaffScanActivity.class);
                intent.putExtra("scannedContent", result.getText());
                startActivity(intent);
            } else {
                Toast.makeText(this, "No QR code found in image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to decode image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditProfileDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        dialogAvatarImage = view.findViewById(R.id.imgDialogAvatar);
        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etDob = view.findViewById(R.id.etDateOfBirth);
        Spinner spinnerGender = view.findViewById(R.id.spinnerGender);
        EditText etAddress = view.findViewById(R.id.etAddress);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Not specified", "Female", "Male", "Other"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        if (currentUser != null) {
            etName.setText(currentUser.getName());
            etPhone.setText(currentUser.getPhone());
            etDob.setText(currentUser.getDateOfBirth());
            etAddress.setText(currentUser.getAddress());
            if (currentUser.getGender() != null) {
                int genderPosition = genderAdapter.getPosition(currentUser.getGender());
                if (genderPosition >= 0) spinnerGender.setSelection(genderPosition);
            }
            loadAvatarInto(dialogAvatarImage, currentUser.getAvatarUrl());
        }

        view.findViewById(R.id.btnChangeAvatar).setOnClickListener(v -> avatarLauncher.launch("image/*"));
        view.findViewById(R.id.btnChangePassword).setOnClickListener(v -> sendPasswordResetEmail());
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = value(etName);
            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            saveProfile(name, value(etPhone), value(etAddress), value(etDob),
                    spinnerGender.getSelectedItem().toString());
            dialog.dismiss();
        });

        dialog.setOnDismissListener(d -> dialogAvatarImage = null);
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void saveProfile(String name, String phone, String address, String dateOfBirth, String gender) {
        if (userId.isEmpty()) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("address", address);
        updates.put("dateOfBirth", dateOfBirth);
        updates.put("gender", gender);

        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    // Update local state
                    if (currentUser != null) {
                        currentUser.setName(name);
                        currentUser.setPhone(phone);
                        currentUser.setAddress(address);
                        currentUser.setDateOfBirth(dateOfBirth);
                        currentUser.setGender(gender);
                        populateProfile(currentUser);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadAvatar(Uri uri) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dialogAvatarImage != null) {
            dialogAvatarImage.setImageURI(uri);
        }
        binding.imgAvatar.setImageURI(uri);

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("user_avatars")
                .child(userId + ".jpg");
        ref.putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    String avatarUrl = downloadUri.toString();
                    db.collection("users").document(userId).update("avatarUrl", avatarUrl)
                            .addOnSuccessListener(unused -> {
                                if (currentUser != null) currentUser.setAvatarUrl(avatarUrl);
                                loadAvatarInto(binding.imgAvatar, avatarUrl);
                                if (dialogAvatarImage != null) loadAvatarInto(dialogAvatarImage, avatarUrl);
                                Toast.makeText(this, "Avatar updated", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Avatar upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendPasswordResetEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user != null ? user.getEmail() : (currentUser != null ? currentUser.getEmail() : null);
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "No email is attached to this account", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Unable to send reset email", Toast.LENGTH_SHORT).show());
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadProfile() {
        if (userId.isEmpty()) return;
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentUser = doc.toObject(User.class);
                        if (currentUser != null) populateProfile(currentUser);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void populateProfile(User user) {
        binding.tvName.setText(user.getName() != null ? user.getName() : "—");
        binding.tvPatientId.setText("Patient ID: " + (user.getPatientId() != null ? user.getPatientId() : "N/A"));
        binding.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "—");
        binding.tvPhone.setText(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "Not set");
        binding.tvAddress.setText(user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "Not set");
        String gender = user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "Not specified";
        String dob = user.getDateOfBirth() != null && !user.getDateOfBirth().isEmpty() ? user.getDateOfBirth() : "Birthday not set";
        binding.tvProfileMeta.setText(gender + " - " + dob);
        loadAvatarInto(binding.imgAvatar, user.getAvatarUrl());

        String role = user.getRole();
        if (role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("staff"))) {
            binding.cardAdministrative.setVisibility(View.VISIBLE);
        } else {
            binding.cardAdministrative.setVisibility(View.GONE);
        }
    }

    private void loadAvatarInto(ImageView imageView, String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this).load(avatarUrl).centerCrop().placeholder(R.drawable.ic_profile).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_profile);
        }
    }

    private String value(EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_profile);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_appointments) {
                startActivity(new Intent(this, AppointmentsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_records) {
                startActivity(new Intent(this, MedicalRecordsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) return true;
            return false;
        });
    }
}
