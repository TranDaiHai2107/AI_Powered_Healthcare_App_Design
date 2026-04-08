package com.healthcare.app.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.healthcare.app.adapter.NotificationAdapter;
import com.healthcare.app.databinding.ActivityNotificationsBinding;
import com.healthcare.app.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private FirebaseFirestore db;
    private NotificationAdapter adapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        setupRecyclerView();
        setupClickListeners();
        loadNotifications();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this, new ArrayList<>());
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }

    private void loadNotifications() {
        db.collection("notifications").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) { notifications.add(doc.toObject(Notification.class)); }
                    adapter.updateList(notifications);
                    updateUnreadCount();
                })
                .addOnFailureListener(e -> { Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show(); adapter.updateList(null); updateUnreadCount(); });
    }

    private void updateUnreadCount() {
        long unread = adapter.getUnreadCount();
        binding.tvUnreadCount.setText(unread + " unread notification" + (unread != 1 ? "s" : ""));
    }

    private void markAllAsRead() {
        db.collection("notifications").whereEqualTo("userId", userId).whereEqualTo("isRead", false).get()
                .addOnSuccessListener(querySnapshot -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : querySnapshot) { batch.update(doc.getReference(), "isRead", true); }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> { Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show(); loadNotifications(); })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to mark notifications", Toast.LENGTH_SHORT).show());
                });
    }
}
