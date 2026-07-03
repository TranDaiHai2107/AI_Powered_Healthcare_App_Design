package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityBookingBinding;
import com.healthcare.app.model.Doctor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import android.widget.ArrayAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.healthcare.app.model.FamilyMember;

public class BookingActivity extends AppCompatActivity {

    private ActivityBookingBinding binding;
    private FirebaseFirestore db;
    private String doctorId;
    private Doctor currentDoctor;
    private String todayDateStr;

    private int currentStep = 1;
    private String selectedService = null;
    private String selectedDate = null;
    private String selectedTime = null;

    private final View[] stepCircles = new View[4];
    private final View[] stepIcons = new View[4];
    private final TextView[] stepLabels = new TextView[4];
    private final View[] stepLines = new View[3];

    private MaterialButton lastSelectedServiceBtn = null;
    private View lastSelectedDateView = null;
    private MaterialButton lastSelectedTimeBtn = null;

    private List<FamilyMember> familyMembers = new ArrayList<>();
    private String selectedPatientName = "Self";
    private String selectedFamilyMemberId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        doctorId = getIntent().getStringExtra("doctorId");

        initStepViews();
        setupClickListeners();
        setupServiceButtons();
        setupDateButtons();
        loadDoctor();
        loadFamilyMembers();
    }

    private void initStepViews() {
        stepCircles[0] = binding.stepCircle1; stepCircles[1] = binding.stepCircle2;
        stepCircles[2] = binding.stepCircle3; stepCircles[3] = binding.stepCircle4;
        stepIcons[0] = binding.stepIcon1; stepIcons[1] = binding.stepIcon2;
        stepIcons[2] = binding.stepIcon3; stepIcons[3] = binding.stepIcon4;
        stepLabels[0] = binding.stepLabel1; stepLabels[1] = binding.stepLabel2;
        stepLabels[2] = binding.stepLabel3; stepLabels[3] = binding.stepLabel4;
        stepLines[0] = binding.stepLine1; stepLines[1] = binding.stepLine2; stepLines[2] = binding.stepLine3;
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> { if (currentStep > 1) { currentStep--; updateStep(); } else finish(); });
        binding.btnContinue.setOnClickListener(v -> {
            if (canProceed()) { if (currentStep < 4) { currentStep++; updateStep(); } else navigateToPayment(); }
            else showValidationError();
        });
    }

    private void setupServiceButtons() {
        MaterialButton[] serviceBtns = { binding.btnServiceGeneral, binding.btnServiceSpecialist, binding.btnServiceFollowUp, binding.btnServiceEmergency };
        String[] serviceNames = { "General Checkup", "Specialist Consultation", "Follow-up Visit", "Emergency" };
        for (int i = 0; i < serviceBtns.length; i++) {
            final String serviceName = serviceNames[i]; final MaterialButton btn = serviceBtns[i];
            btn.setOnClickListener(v -> {
                if (lastSelectedServiceBtn != null) { lastSelectedServiceBtn.setBackgroundColor(getResources().getColor(R.color.white, null)); lastSelectedServiceBtn.setStrokeColorResource(R.color.border_color); }
                btn.setBackgroundColor(getResources().getColor(R.color.pastel_blue, null)); btn.setStrokeWidth(0);
                lastSelectedServiceBtn = btn; selectedService = serviceName;
            });
        }
    }

    private void setupDateButtons() {
        binding.layoutDateButtons.removeAllViews();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.US);
        SimpleDateFormat dateNumFormat = new SimpleDateFormat("dd", Locale.US);
        SimpleDateFormat fullFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        todayDateStr = fullFormat.format(calendar.getTime());

        for (int i = 0; i < 5; i++) {
            Calendar day = (Calendar) calendar.clone(); day.add(Calendar.DAY_OF_MONTH, i);
            String dayName = (i == 0) ? "Today" : dayFormat.format(day.getTime());
            String dateNum = dateNumFormat.format(day.getTime());
            String fullDate = fullFormat.format(day.getTime());

            LinearLayout dateItem = new LinearLayout(this);
            dateItem.setOrientation(LinearLayout.VERTICAL); dateItem.setGravity(Gravity.CENTER);
            dateItem.setBackground(getDrawable(R.drawable.bg_date_unselected));
            dateItem.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(72), LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0); dateItem.setLayoutParams(params);

            TextView tvDay = new TextView(this); tvDay.setText(dayName); tvDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvDay.setTextColor(getResources().getColor(R.color.healthcare_muted, null)); tvDay.setGravity(Gravity.CENTER);
            TextView tvDate = new TextView(this); tvDate.setText(dateNum); tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            tvDate.setTextColor(getResources().getColor(R.color.healthcare_dark, null)); tvDate.setTypeface(null, android.graphics.Typeface.BOLD); tvDate.setGravity(Gravity.CENTER);
            dateItem.addView(tvDay); dateItem.addView(tvDate);

            if (i == 0) {
                dateItem.setBackground(getDrawable(R.drawable.bg_date_selected));
                tvDay.setTextColor(getResources().getColor(R.color.healthcare_dark, null));
                lastSelectedDateView = dateItem; selectedDate = fullDate;
            }

            dateItem.setOnClickListener(v -> {
                if (lastSelectedDateView != null) { lastSelectedDateView.setBackground(getDrawable(R.drawable.bg_date_unselected)); resetDateTextColors(lastSelectedDateView); }
                dateItem.setBackground(getDrawable(R.drawable.bg_date_selected));
                tvDay.setTextColor(getResources().getColor(R.color.healthcare_dark, null));
                lastSelectedDateView = dateItem; selectedDate = fullDate;

                selectedTime = null;
                lastSelectedTimeBtn = null;
                if (currentDoctor != null) {
                    loadBookedSlotsAndPopulate(currentDoctor.getAvailableSlotsArray());
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

    private void loadBookedSlotsAndPopulate(String[] slots) {
        String uid = FirebaseAuth.getInstance().getUid();
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
                                    if (status != null && !status.equalsIgnoreCase("cancelled")) {
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
            String trimmed = slot.trim(); if (trimmed.isEmpty()) continue;
            MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setText(trimmed); btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13); btn.setCornerRadius(dpToPx(16));
            btn.setAllCaps(false);
            btn.setMinHeight(dpToPx(40)); btn.setMinimumHeight(dpToPx(40)); btn.setPadding(dpToPx(8), 0, dpToPx(8), 0);

            GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
            gridParams.width = 0; gridParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            gridParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            gridParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4)); gridParams.setGravity(Gravity.FILL_HORIZONTAL);
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
                    if (lastSelectedTimeBtn != null) { lastSelectedTimeBtn.setBackgroundColor(getResources().getColor(R.color.white, null)); }
                    btn.setBackgroundColor(getResources().getColor(R.color.pastel_blue, null)); lastSelectedTimeBtn = btn; selectedTime = trimmed;
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

    private void loadDoctor() {
        if (doctorId == null) { Toast.makeText(this, "Invalid doctor", Toast.LENGTH_SHORT).show(); finish(); return; }
        db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentDoctor = doc.toObject(Doctor.class);
                        if (currentDoctor != null) {
                            populateUI(currentDoctor);
                            if (currentDoctor.getAvailableSlotsArray() != null) {
                                loadBookedSlotsAndPopulate(currentDoctor.getAvailableSlotsArray());
                            }
                        }
                    }
                    else Toast.makeText(this, "Doctor not found", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load doctor details", Toast.LENGTH_SHORT).show());
    }

    private void loadFamilyMembers() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("family_members").whereEqualTo("userId", uid).get()
                .addOnSuccessListener(querySnapshot -> {
                    familyMembers.clear();
                    List<String> patientNames = new ArrayList<>();
                    patientNames.add("Self"); // Default option

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        FamilyMember member = doc.toObject(FamilyMember.class);
                        familyMembers.add(member);
                        patientNames.add(member.getName() + " (" + member.getRelationship() + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, patientNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerPatient.setAdapter(adapter);
                });
    }

    private void populateUI(Doctor doctor) {
        binding.tvDoctorName.setText(doctor.getName()); binding.tvSpecialization.setText(doctor.getSpecialization());
        if (doctor.getConsultationFee() != null) binding.tvFee.setText(String.format(Locale.US, "$%.0f", doctor.getConsultationFee()));
        Glide.with(this).load(doctor.getImage()).centerCrop().placeholder(R.drawable.bg_rounded_image).into(binding.imgDoctor);
    }

    private void updateStep() {
        binding.layoutStep1.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        binding.layoutStep2.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        binding.layoutStep3.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);
        binding.layoutStep4.setVisibility(currentStep == 4 ? View.VISIBLE : View.GONE);
        for (int i = 0; i < 4; i++) {
            if (i < currentStep - 1) { stepCircles[i].setBackgroundResource(R.drawable.bg_step_completed); stepLabels[i].setTextColor(getResources().getColor(R.color.pastel_mint_dark, null)); }
            else if (i == currentStep - 1) { stepCircles[i].setBackgroundResource(R.drawable.bg_step_active); stepLabels[i].setTextColor(getResources().getColor(R.color.healthcare_dark, null)); }
            else { stepCircles[i].setBackgroundResource(R.drawable.bg_step_inactive); stepLabels[i].setTextColor(getResources().getColor(R.color.healthcare_muted, null)); }
        }
        for (int i = 0; i < 3; i++) stepLines[i].setBackgroundResource(i < currentStep - 1 ? R.drawable.bg_step_line_active : R.drawable.bg_step_line_inactive);
        if (currentStep == 4) { binding.btnContinue.setText("Proceed to Payment"); populateSummary(); } else binding.btnContinue.setText("Continue");
    }

    private void populateSummary() {
        binding.tvSummaryService.setText(selectedService != null ? selectedService : "—");
        binding.tvSummaryDate.setText(selectedDate != null ? selectedDate : "—");
        binding.tvSummaryTime.setText(selectedTime != null ? selectedTime : "—");
        if (currentDoctor != null && currentDoctor.getConsultationFee() != null)
            binding.tvSummaryTotal.setText(String.format(Locale.US, "$%.0f", currentDoctor.getConsultationFee()));
    }

    private boolean canProceed() {
        switch (currentStep) { case 1: return selectedService != null; case 2: return selectedDate != null && selectedTime != null; default: return true; }
    }

    private void showValidationError() {
        if (currentStep == 1) Toast.makeText(this, "Please select a service type", Toast.LENGTH_SHORT).show();
        else if (currentStep == 2) Toast.makeText(this, selectedDate == null ? "Please select a date" : "Please select a time slot", Toast.LENGTH_SHORT).show();
    }

    private void navigateToPayment() {
        if (currentDoctor == null) return;

        int selectedPatientPos = binding.spinnerPatient.getSelectedItemPosition();
        if (selectedPatientPos > 0 && selectedPatientPos <= familyMembers.size()) {
            FamilyMember selectedMember = familyMembers.get(selectedPatientPos - 1);
            selectedPatientName = selectedMember.getName();
            selectedFamilyMemberId = selectedMember.getDocumentId();
        } else {
            selectedPatientName = "Self";
            selectedFamilyMemberId = null;
        }

        String symptoms = binding.etSymptoms.getText() != null ? binding.etSymptoms.getText().toString().trim() : "";

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("doctorId", currentDoctor.getDocumentId());
        intent.putExtra("selectedService", selectedService);
        intent.putExtra("selectedDate", selectedDate);
        intent.putExtra("selectedTime", selectedTime);
        intent.putExtra("doctorName", currentDoctor.getName());
        intent.putExtra("doctorSpecialization", currentDoctor.getSpecialization());
        intent.putExtra("doctorImage", currentDoctor.getImage());
        intent.putExtra("hospitalName", currentDoctor.getHospitalName());
        intent.putExtra("patientName", selectedPatientName);
        intent.putExtra("familyMemberId", selectedFamilyMemberId);
        intent.putExtra("symptoms", symptoms);
        startActivity(intent);
    }

    private int dpToPx(int dp) { return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()); }
}
