package com.healthcare.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ItemHospitalBinding;
import com.healthcare.app.model.Hospital;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    private final Context context;
    private List<Hospital> hospitals;
    private final OnHospitalClickListener listener;

    public interface OnHospitalClickListener {
        void onHospitalClick(Hospital hospital);
    }

    public HospitalAdapter(Context context, List<Hospital> hospitals, OnHospitalClickListener listener) {
        this.context = context;
        this.hospitals = hospitals != null ? hospitals : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHospitalBinding binding = ItemHospitalBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HospitalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        holder.bind(hospitals.get(position));
    }

    @Override
    public int getItemCount() {
        return hospitals.size();
    }

    public void updateList(List<Hospital> newHospitals) {
        this.hospitals = newHospitals != null ? newHospitals : new ArrayList<>();
        notifyDataSetChanged();
    }

    class HospitalViewHolder extends RecyclerView.ViewHolder {

        private final ItemHospitalBinding binding;

        HospitalViewHolder(ItemHospitalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Hospital hospital) {
            binding.tvHospitalName.setText(hospital.getName());

            if (hospital.getRating() != null) {
                binding.tvRating.setText(String.format(Locale.US, "%.1f", hospital.getRating()));
            }

            binding.tvDistance.setText(
                    hospital.getDistance() != null ? hospital.getDistance() : "N/A");

            binding.tvPriceRange.setText(
                    hospital.getPriceRange() != null ? hospital.getPriceRange() : "");

            Glide.with(context)
                    .load(hospital.getImage())
                    .centerCrop()
                    .placeholder(R.drawable.bg_rounded_image)
                    .into(binding.imgHospital);

            binding.chipGroupSpecialties.removeAllViews();
            String[] specialties = hospital.getSpecialtiesArray();
            for (String specialty : specialties) {
                String trimmed = specialty.trim();
                if (!trimmed.isEmpty()) {
                    Chip chip = new Chip(context);
                    chip.setText(trimmed);
                    chip.setTextSize(11);
                    chip.setChipBackgroundColorResource(R.color.pastel_blue_10);
                    chip.setTextColor(context.getResources().getColor(R.color.healthcare_dark, null));
                    chip.setClickable(false);
                    chip.setCheckable(false);
                    chip.setChipMinHeight(28f);
                    binding.chipGroupSpecialties.addView(chip);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHospitalClick(hospital);
                }
            });
        }
    }
}
