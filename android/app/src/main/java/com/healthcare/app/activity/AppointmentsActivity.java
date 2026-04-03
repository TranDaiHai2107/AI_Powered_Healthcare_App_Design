package com.healthcare.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.healthcare.app.R;
import com.healthcare.app.adapter.AppointmentAdapter;
import com.healthcare.app.databinding.ActivityAppointmentsBinding;
import com.healthcare.app.model.Appointment;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsActivity extends AppCompatActivity {

    private ActivityAppointmentsBinding binding;
    private FirebaseFirestore db;
    private AppointmentAdapter adapter;
    private String userId;
    private String currentStatus = "upcoming";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        setupRecyclerView();
        setupTabs();
        setupBottomNav();
        loadAppointments("upcoming");
    }

    private void setupRecyclerView() {
        adapter = new AppointmentAdapter(this, new ArrayList<>(), new AppointmentAdapter.OnAppointmentClickListener() {
            @Override public void onCheckIn(String appointmentId) {
                Intent intent = new Intent(AppointmentsActivity.this, CheckInActivity.class);
                intent.putExtra("appointmentId", appointmentId); startActivity(intent);
            }
            @Override public void onViewRecords() { startActivity(new Intent(AppointmentsActivity.this, MedicalRecordsActivity.class)); }
        });
        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAppointments.setAdapter(adapter);
    }

    private void setupTabs() {
        selectTab(binding.btnUpcoming);
        binding.btnUpcoming.setOnClickListener(v -> { currentStatus = "upcoming"; selectTab(binding.btnUpcoming); loadAppointments("upcoming"); });
        binding.btnCompleted.setOnClickListener(v -> { currentStatus = "completed"; selectTab(binding.btnCompleted); loadAppointments("completed"); });
        binding.btnCancelled.setOnClickListener(v -> { currentStatus = "cancelled"; selectTab(binding.btnCancelled); loadAppointments("cancelled"); });
    }

    private void selectTab(MaterialButton selected) {
        MaterialButton[] tabs = {binding.btnUpcoming, binding.btnCompleted, binding.btnCancelled};
        for (MaterialButton tab : tabs) {
            if (tab == selected) { tab.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.pastel_blue)); tab.setTextColor(ContextCompat.getColor(this, R.color.white)); }
            else { tab.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white)); tab.setTextColor(ContextCompat.getColor(this, R.color.healthcare_muted)); }
        }
    }

    private void loadAppointments(String status) {
        db.collection("appointments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Appointment> appointments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) { appointments.add(doc.toObject(Appointment.class)); }
                    adapter.updateList(appointments);
                    updateEmptyState(appointments, status);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
                    adapter.updateList(null); updateEmptyState(null, status);
                });
    }

    private void updateEmptyState(List<Appointment> appointments, String status) {
        boolean isEmpty = appointments == null || appointments.isEmpty();
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvAppointments.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyMessage.setText("No " + status + " appointments");
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_appointments);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { startActivity(new Intent(this, HomeActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_search) { startActivity(new Intent(this, SearchActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_appointments) return true;
            else if (id == R.id.nav_records) { startActivity(new Intent(this, MedicalRecordsActivity.class)); overridePendingTransition(0, 0); return true; }
            else if (id == R.id.nav_profile) { startActivity(new Intent(this, ProfileActivity.class)); overridePendingTransition(0, 0); return true; }
            return false;
        });
    }
}
