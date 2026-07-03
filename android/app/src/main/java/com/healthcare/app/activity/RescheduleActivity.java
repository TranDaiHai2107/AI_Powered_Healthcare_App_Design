package com.healthcare.app.activity;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityRescheduleBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class RescheduleActivity extends AppCompatActivity {

    private ActivityRescheduleBinding binding;
    private FirebaseFirestore db;
    private String appointmentId;
    private String doctorId;
    private String currentDate;
    private String currentTime;

    private String selectedDate = null;
    private String selectedTime = null;
    private String todayDateStr;
    private String doctorSlotsStr = null;

    private View lastSelectedDateView = null;
    private MaterialButton lastSelectedTimeBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRescheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        appointmentId = getIntent().getStringExtra("appointmentId");
        doctorId = getIntent().getStringExtra("doctorId");
        currentDate = getIntent().getStringExtra("currentDate");
        currentTime = getIntent().getStringExtra("currentTime");

        if (appointmentId == null) {
            Toast.makeText(this, "Invalid appointment", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.tvCurrentDate.setText(currentDate + " at " + currentTime);

        setupClickListeners();
        setupDateButtons();
        loadDoctorSlots();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnConfirmReschedule.setOnClickListener(v -> confirmReschedule());
    }

    private void setupDateButtons() {
        binding.layoutDateButtons.removeAllViews();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.US);
        SimpleDateFormat dateNumFormat = new SimpleDateFormat("dd", Locale.US);
        SimpleDateFormat fullFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        todayDateStr = fullFormat.format(calendar.getTime());

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) calendar.clone();
            day.add(Calendar.DAY_OF_MONTH, i);
            String dayName = (i == 0) ? "Today" : dayFormat.format(day.getTime());
            String dateNum = dateNumFormat.format(day.getTime());
            String fullDate = fullFormat.format(day.getTime());

            LinearLayout dateItem = new LinearLayout(this);
            dateItem.setOrientation(LinearLayout.VERTICAL);
            dateItem.setGravity(Gravity.CENTER);
            dateItem.setBackground(getDrawable(R.drawable.bg_date_unselected));
            dateItem.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(72), LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dateItem.setLayoutParams(params);

            TextView tvDay = new TextView(this);
            tvDay.setText(dayName);
            tvDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvDay.setTextColor(getResources().getColor(R.color.healthcare_muted, null));
            tvDay.setGravity(Gravity.CENTER);

            TextView tvDate = new TextView(this);
            tvDate.setText(dateNum);
            tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            tvDate.setTextColor(getResources().getColor(R.color.healthcare_dark, null));
            tvDate.setTypeface(null, android.graphics.Typeface.BOLD);
            tvDate.setGravity(Gravity.CENTER);

            dateItem.addView(tvDay);
            dateItem.addView(tvDate);

            if (i == 0) {
                dateItem.setBackground(getDrawable(R.drawable.bg_date_selected));
                tvDay.setTextColor(getResources().getColor(R.color.healthcare_dark, null));
                lastSelectedDateView = dateItem; selectedDate = fullDate;
            }

            dateItem.setOnClickListener(v -> {
                if (lastSelectedDateView != null) {
                    lastSelectedDateView.setBackground(getDrawable(R.drawable.bg_date_unselected));
                    resetDateTextColors(lastSelectedDateView);
                }
                dateItem.setBackground(getDrawable(R.drawable.bg_date_selected));
                tvDay.setTextColor(getResources().getColor(R.color.healthcare_dark, null));
                lastSelectedDateView = dateItem;
                selectedDate = fullDate;

                selectedTime = null;
                lastSelectedTimeBtn = null;
                if (doctorSlotsStr != null) {
                    loadBookedSlotsAndPopulate(doctorSlotsStr.split(","));
                }
            });
            binding.layoutDateButtons.addView(dateItem);
        }
    }

    private void resetDateTextColors(View dateView) {
        if (dateView instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) dateView;
            if (layout.getChildCount() >= 2) {
                ((TextView) layout.getChildAt(0)).setTextColor(getResources().getColor(R.color.healthcare_muted, null));
                ((TextView) layout.getChildAt(1)).setTextColor(getResources().getColor(R.color.healthcare_dark, null));
            }
        }
    }

    private void loadDoctorSlots() {
        if (doctorId == null) return;
        db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        doctorSlotsStr = doc.getString("availableSlots");
                        if (doctorSlotsStr != null) {
                            loadBookedSlotsAndPopulate(doctorSlotsStr.split(","));
                        }
                    }
                });
    }

    private void loadBookedSlotsAndPopulate(String[] slots) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (doctorId == null || selectedDate == null || uid == null) {
            populateTimeSlots(slots, new HashMap<>(), new ArrayList<>());
            return;
        }

        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener(doctorSnapshot -> {
                    Map<String, Integer> bookedCounts = new HashMap<>();
                    for (QueryDocumentSnapshot doc : doctorSnapshot) {
                        String status = doc.getString("status");
                        if (status != null && !status.equalsIgnoreCase("cancelled")) {
                            String time = doc.getString("time");
                            if (time != null) {
                                String trimmedTime = time.trim();
                                bookedCounts.put(trimmedTime, bookedCounts.getOrDefault(trimmedTime, 0) + 1);
                            }
                        }
                    }

                    db.collection("appointments")
                            .whereEqualTo("userId", uid)
                            .whereEqualTo("date", selectedDate)
                            .get()
                            .addOnSuccessListener(userSnapshot -> {
                                List<String> userBookedTimes = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : userSnapshot) {
                                    String status = doc.getString("status");
                                    String id = doc.getString("appointmentId");
                                    if (status != null && !status.equalsIgnoreCase("cancelled")
                                            && (id == null || !id.equals(appointmentId))) {
                                        String time = doc.getString("time");
                                        if (time != null) {
                                            userBookedTimes.add(time.trim());
                                        }
                                    }
                                }
                                populateTimeSlots(slots, bookedCounts, userBookedTimes);
                            })
                            .addOnFailureListener(e -> {
                                populateTimeSlots(slots, bookedCounts, new ArrayList<>());
                            });
                })
                .addOnFailureListener(e -> {
                    populateTimeSlots(slots, new HashMap<>(), new ArrayList<>());
                });
    }

    private void populateTimeSlots(String[] slots, Map<String, Integer> bookedCounts, List<String> userBookedTimes) {
        binding.gridTimeSlots.removeAllViews();
        if (slots == null || slots.length == 0) return;

        boolean isToday = selectedDate != null && selectedDate.equals(todayDateStr);

        for (String slot : slots) {
            String trimmed = slot.trim();
            if (trimmed.isEmpty()) continue;
            MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setText(trimmed);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            btn.setCornerRadius(dpToPx(16));
            btn.setAllCaps(false);
            btn.setMinHeight(dpToPx(40));
            btn.setMinimumHeight(dpToPx(40));
            btn.setPadding(dpToPx(8), 0, dpToPx(8), 0);

            GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
            gridParams.width = 0;
            gridParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            gridParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            gridParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            gridParams.setGravity(Gravity.FILL_HORIZONTAL);
            btn.setLayoutParams(gridParams);

            boolean isPast = isToday && isTimeInPast(trimmed);
            int bookedCount = bookedCounts.getOrDefault(trimmed, 0);
            boolean isFull = bookedCount >= 5;
            boolean isUserAlreadyBooked = false;
            for (String userTime : userBookedTimes) {
                if (userTime.equalsIgnoreCase(trimmed)) {
                    isUserAlreadyBooked = true;
                    break;
                }
            }

            if (isPast || isFull || isUserAlreadyBooked) {
                btn.setEnabled(false);
                btn.setTextColor(getResources().getColor(R.color.healthcare_muted, null));
                btn.setStrokeColorResource(R.color.border_color);
                btn.setBackgroundColor(getResources().getColor(R.color.healthcare_gray, null));
                if (isFull) {
                    btn.setText(trimmed + " (Full)");
                } else if (isUserAlreadyBooked) {
                    btn.setText(trimmed + " (Booked)");
                }
            } else {
                btn.setEnabled(true);
                btn.setTextColor(getResources().getColor(R.color.healthcare_dark, null));
                btn.setStrokeColorResource(R.color.border_color);
                btn.setBackgroundColor(getResources().getColor(R.color.white, null));
                btn.setOnClickListener(v -> {
                    if (lastSelectedTimeBtn != null) {
                        lastSelectedTimeBtn.setBackgroundColor(getResources().getColor(R.color.white, null));
                    }
                    btn.setBackgroundColor(getResources().getColor(R.color.pastel_blue, null));
                    lastSelectedTimeBtn = btn;
                    selectedTime = trimmed;
                });
            }
            binding.gridTimeSlots.addView(btn);
        }
    }

    private boolean isTimeInPast(String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
            Calendar slotTime = Calendar.getInstance();
            slotTime.setTime(sdf.parse(timeStr.trim()));

            Calendar currentTime = Calendar.getInstance();

            slotTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR));
            slotTime.set(Calendar.MONTH, currentTime.get(Calendar.MONTH));
            slotTime.set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH));

            return slotTime.before(currentTime);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void confirmReschedule() {
        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Please select new date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnConfirmReschedule.setEnabled(false);

        // 1. Double check doctor capacity for the new slot
        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", selectedDate)
                .whereEqualTo("time", selectedTime)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int bookedCount = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String status = doc.getString("status");
                        if (status != null && !status.equalsIgnoreCase("cancelled")) {
                            bookedCount++;
                        }
                    }

                    if (bookedCount >= 5) {
                        binding.btnConfirmReschedule.setEnabled(true);
                        Toast.makeText(this, "This slot has just reached its maximum capacity of 5 patients. Please choose a different time.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // 2. Double check if this user already has an appointment at this time on this date
                    // (ignoring the current appointment being rescheduled)
                    String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                    if (uid == null) {
                        binding.btnConfirmReschedule.setEnabled(true);
                        Toast.makeText(this, "Session expired, please login.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("appointments")
                            .whereEqualTo("userId", uid)
                            .whereEqualTo("date", selectedDate)
                            .whereEqualTo("time", selectedTime)
                            .get()
                            .addOnSuccessListener(userSnapshot -> {
                                boolean hasSelfBooking = false;
                                for (QueryDocumentSnapshot doc : userSnapshot) {
                                    String status = doc.getString("status");
                                    String id = doc.getString("appointmentId");
                                    if (status != null && !status.equalsIgnoreCase("cancelled")
                                            && (id == null || !id.equals(appointmentId))) {
                                        hasSelfBooking = true;
                                        break;
                                    }
                                }

                                if (hasSelfBooking) {
                                    binding.btnConfirmReschedule.setEnabled(true);
                                    Toast.makeText(this, "You already have another appointment scheduled at this time.", Toast.LENGTH_LONG).show();
                                } else {
                                    // 3. Actually reschedule
                                    db.collection("appointments").whereEqualTo("appointmentId", appointmentId).get()
                                            .addOnSuccessListener(aptSnapshot -> {
                                                if (!aptSnapshot.isEmpty()) {
                                                    String docId = aptSnapshot.getDocuments().get(0).getId();
                                                    db.collection("appointments").document(docId)
                                                            .update("date", selectedDate, "time", selectedTime)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(this, "Appointment rescheduled successfully!", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                binding.btnConfirmReschedule.setEnabled(true);
                                                                Toast.makeText(this, "Failed to reschedule: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                } else {
                                                    binding.btnConfirmReschedule.setEnabled(true);
                                                    Toast.makeText(this, "Appointment record not found.", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                binding.btnConfirmReschedule.setEnabled(true);
                                                Toast.makeText(this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                binding.btnConfirmReschedule.setEnabled(true);
                                Toast.makeText(this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    binding.btnConfirmReschedule.setEnabled(true);
                    Toast.makeText(this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
