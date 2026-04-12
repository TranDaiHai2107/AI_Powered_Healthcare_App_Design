package com.healthcare.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.healthcare.app.R;
import com.healthcare.app.adapter.RecordAdapter;
import com.healthcare.app.databinding.ActivityMedicalRecordsBinding;
import com.healthcare.app.model.MedicalRecord;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordsActivity extends AppCompatActivity {

    private ActivityMedicalRecordsBinding binding;
    private FirebaseFirestore db;
    private RecordAdapter adapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicalRecordsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        setupRecyclerView();
        setupChips();
        setupBottomNav();
        loadRecords(null);
    }

    private void setupRecyclerView() {
        adapter = new RecordAdapter(this, new ArrayList<>());
        binding.rvRecords.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecords.setAdapter(adapter);
    }

    private void setupChips() {
        selectChip(binding.chipAll);
        binding.chipAll.setOnClickListener(v -> { selectChip(binding.chipAll); loadRecords(null); });
        binding.chipAppointments.setOnClickListener(v -> { selectChip(binding.chipAppointments); loadRecords("appointment"); });
        binding.chipPrescriptions.setOnClickListener(v -> { selectChip(binding.chipPrescriptions); loadRecords("prescription"); });
        binding.chipLabResults.setOnClickListener(v -> { selectChip(binding.chipLabResults); loadRecords("lab-result"); });
        binding.chipNotes.setOnClickListener(v -> { selectChip(binding.chipNotes); loadRecords("note"); });
    }

    private void selectChip(MaterialButton selected) {
        MaterialButton[] chips = { binding.chipAll, binding.chipAppointments, binding.chipPrescriptions, binding.chipLabResults, binding.chipNotes };
        for (MaterialButton chip : chips) {
            if (chip == selected) { chip.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.pastel_blue)); chip.setTextColor(ContextCompat.getColor(this, R.color.white)); }
            else { chip.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white)); chip.setTextColor(ContextCompat.getColor(this, R.color.healthcare_muted)); }
        }
    }

    private void loadRecords(String type) {
        Query query = db.collection("medical_records").whereEqualTo("userId", userId);
        if (type != null) query = query.whereEqualTo("type", type);

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<MedicalRecord> records = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) { records.add(doc.toObject(MedicalRecord.class)); }
                    adapter.updateList(records);
                })
                .addOnFailureListener(e -> { Toast.makeText(this, "Failed to load records", Toast.LENGTH_SHORT).show(); adapter.updateList(null); });
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_records);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { startActivity(new Intent(this, HomeActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_search) { startActivity(new Intent(this, SearchActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_appointments) { startActivity(new Intent(this, AppointmentsActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_records) return true;
            else if (id == R.id.nav_profile) { startActivity(new Intent(this, ProfileActivity.class)); overridePendingTransition(0, 0); return true; }
            return false;
        });
    }
}
