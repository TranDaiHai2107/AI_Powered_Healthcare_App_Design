package com.healthcare.app.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ItemAppointmentBinding;
import com.healthcare.app.model.Appointment;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private final Context context;
    private List<Appointment> appointments;
    private final OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onCheckIn(String appointmentId);
        void onViewRecords();
    }

    public AppointmentAdapter(Context context, List<Appointment> appointments, OnAppointmentClickListener listener) {
        this.context = context;
        this.appointments = appointments != null ? appointments : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppointmentBinding binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppointmentViewHolder(binding);
    }

    @Override public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) { holder.bind(appointments.get(position)); }
    @Override public int getItemCount() { return appointments.size(); }

    public void updateList(List<Appointment> newAppointments) {
        this.appointments = newAppointments != null ? newAppointments : new ArrayList<>();
        notifyDataSetChanged();
    }

    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppointmentBinding binding;
        AppointmentViewHolder(ItemAppointmentBinding binding) { super(binding.getRoot()); this.binding = binding; }

        void bind(Appointment appointment) {
            String status = appointment.getStatus() != null ? appointment.getStatus() : "";
            applyStatusBadge(status);

            binding.tvDoctorName.setText(appointment.getDoctorName());
            binding.tvSpecialization.setText(appointment.getDoctorSpecialization());

            Glide.with(context).load(appointment.getDoctorImage()).centerCrop()
                    .placeholder(R.drawable.bg_rounded_image).into(binding.imgDoctor);

            binding.tvHospital.setText(appointment.getHospital());
            binding.tvDate.setText(appointment.getDate());
            binding.tvTime.setText(appointment.getTime());

            switch (status.toLowerCase()) {
                case "upcoming":
                    binding.btnCheckIn.setVisibility(View.VISIBLE); binding.btnViewRecords.setVisibility(View.GONE); break;
                case "completed":
                    binding.btnCheckIn.setVisibility(View.GONE); binding.btnViewRecords.setVisibility(View.VISIBLE); break;
                default:
                    binding.layoutActions.setVisibility(View.GONE); break;
            }

            binding.btnCheckIn.setOnClickListener(v -> { if (listener != null) listener.onCheckIn(appointment.getAppointmentId()); });
            binding.btnViewRecords.setOnClickListener(v -> { if (listener != null) listener.onViewRecords(); });
        }

        private void applyStatusBadge(String status) {
            int bgColor, textColor; String label;
            switch (status.toLowerCase()) {
                case "completed": bgColor = ContextCompat.getColor(context, R.color.pastel_mint_20); textColor = ContextCompat.getColor(context, R.color.pastel_mint_dark); label = "Completed"; break;
                case "cancelled": bgColor = ContextCompat.getColor(context, R.color.red_100); textColor = ContextCompat.getColor(context, R.color.red_700); label = "Cancelled"; break;
                default: bgColor = ContextCompat.getColor(context, R.color.pastel_blue_20); textColor = ContextCompat.getColor(context, R.color.pastel_blue_dark); label = "Upcoming"; break;
            }
            binding.tvStatus.setText(label); binding.tvStatus.setTextColor(textColor);
            GradientDrawable badge = new GradientDrawable(); badge.setShape(GradientDrawable.RECTANGLE); badge.setCornerRadius(20f); badge.setColor(bgColor);
            binding.tvStatus.setBackground(badge);
        }
    }
}
