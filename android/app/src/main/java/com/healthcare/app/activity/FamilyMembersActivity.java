package com.healthcare.app.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.healthcare.app.R;
import com.healthcare.app.adapter.FamilyMemberAdapter;
import com.healthcare.app.databinding.ActivityFamilyMembersBinding;
import com.healthcare.app.model.FamilyMember;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FamilyMembersActivity extends AppCompatActivity {

    private ActivityFamilyMembersBinding binding;
    private FirebaseFirestore db;
    private FamilyMemberAdapter adapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFamilyMembersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        setupRecyclerView();
        setupClickListeners();
        loadFamilyMembers();
    }

    private void setupRecyclerView() {
        adapter = new FamilyMemberAdapter(this, new ArrayList<>(), new FamilyMemberAdapter.OnFamilyMemberClickListener() {
            @Override
            public void onEdit(FamilyMember member) {
                showAddEditDialog(member);
            }

            @Override
            public void onDelete(FamilyMember member) {
                confirmDelete(member);
            }
        });
        binding.rvFamilyMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFamilyMembers.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.fabAddMember.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void loadFamilyMembers() {
        db.collection("family_members").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<FamilyMember> members = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        members.add(doc.toObject(FamilyMember.class));
                    }
                    adapter.updateList(members);
                    binding.layoutEmpty.setVisibility(members.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load family members", Toast.LENGTH_SHORT).show());
    }

    private void showAddEditDialog(FamilyMember existingMember) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_family_member, null);
        builder.setView(view);
        
        AlertDialog dialog = builder.create();
        
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etName = view.findViewById(R.id.etName);
        Spinner spinnerRelationship = view.findViewById(R.id.spinnerRelationship);
        TextView tvDob = view.findViewById(R.id.tvDob);
        RadioGroup rgGender = view.findViewById(R.id.rgGender);
        RadioButton rbMale = view.findViewById(R.id.rbMale);
        RadioButton rbFemale = view.findViewById(R.id.rbFemale);
        RadioButton rbOther = view.findViewById(R.id.rbOther);
        EditText etPhone = view.findViewById(R.id.etPhone);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.relationship_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelationship.setAdapter(spinnerAdapter);

        tvDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view1, year1, month1, dayOfMonth) -> {
                        String date = String.format(Locale.US, "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                        tvDob.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        if (existingMember != null) {
            tvTitle.setText("Edit Family Member");
            etName.setText(existingMember.getName());
            tvDob.setText(existingMember.getDateOfBirth());
            etPhone.setText(existingMember.getPhone());
            
            if ("Male".equalsIgnoreCase(existingMember.getGender())) rbMale.setChecked(true);
            else if ("Female".equalsIgnoreCase(existingMember.getGender())) rbFemale.setChecked(true);
            else rbOther.setChecked(true);
            
            String rel = existingMember.getRelationship();
            if (rel != null) {
                int spinnerPosition = spinnerAdapter.getPosition(rel);
                if (spinnerPosition >= 0) spinnerRelationship.setSelection(spinnerPosition);
            }
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String relationship = spinnerRelationship.getSelectedItem().toString();
            String dob = tvDob.getText().toString();
            String phone = etPhone.getText().toString().trim();
            
            String gender = "Other";
            if (rbMale.isChecked()) gender = "Male";
            else if (rbFemale.isChecked()) gender = "Female";

            if (name.isEmpty() || dob.equals("Select Date") || dob.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("name", name);
            data.put("relationship", relationship);
            data.put("dateOfBirth", dob);
            data.put("gender", gender);
            data.put("phone", phone);

            if (existingMember == null) {
                db.collection("family_members").add(data)
                        .addOnSuccessListener(ref -> {
                            Toast.makeText(this, "Added successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadFamilyMembers();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to add", Toast.LENGTH_SHORT).show());
            } else {
                db.collection("family_members").document(existingMember.getDocumentId()).update(data)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadFamilyMembers();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void confirmDelete(FamilyMember member) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Member")
                .setMessage("Are you sure you want to delete " + member.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("family_members").document(member.getDocumentId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                loadFamilyMembers();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
