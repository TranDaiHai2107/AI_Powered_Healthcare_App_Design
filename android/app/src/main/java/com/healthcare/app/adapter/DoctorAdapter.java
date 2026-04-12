package com.healthcare.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ItemDoctorBinding;
import com.healthcare.app.model.Doctor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private final Context context;
    private List<Doctor> doctors;
    private final OnDoctorClickListener listener;

    public interface OnDoctorClickListener { void onDoctorClick(Doctor doctor); }

    public DoctorAdapter(Context context, List<Doctor> doctors, OnDoctorClickListener listener) {
        this.context = context;
        this.doctors = doctors != null ? doctors : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DoctorViewHolder(ItemDoctorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) { holder.bind(doctors.get(position)); }
    @Override public int getItemCount() { return doctors.size(); }

    public void updateList(List<Doctor> newDoctors) { this.doctors = newDoctors != null ? newDoctors : new ArrayList<>(); notifyDataSetChanged(); }

    class DoctorViewHolder extends RecyclerView.ViewHolder {
        private final ItemDoctorBinding binding;
        DoctorViewHolder(ItemDoctorBinding binding) { super(binding.getRoot()); this.binding = binding; }

        void bind(Doctor doctor) {
            binding.tvDoctorName.setText(doctor.getName());
            binding.tvSpecialization.setText(doctor.getSpecialization());
            binding.tvHospitalName.setText(doctor.getHospitalName());
            if (doctor.getRating() != null) binding.tvRating.setText(String.format(Locale.US, "%.1f", doctor.getRating()));
            if (doctor.getConsultationFee() != null) binding.tvFee.setText(String.format(Locale.US, "$%.0f", doctor.getConsultationFee()));
            binding.tvNextAvailable.setText(doctor.getNextAvailable() != null ? doctor.getNextAvailable() : "N/A");
            Glide.with(context).load(doctor.getImage()).centerCrop().placeholder(R.drawable.bg_rounded_image).into(binding.imgDoctor);
            itemView.setOnClickListener(v -> { if (listener != null) listener.onDoctorClick(doctor); });
        }
    }
}
