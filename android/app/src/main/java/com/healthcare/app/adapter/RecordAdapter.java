package com.healthcare.app.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.healthcare.app.R;
import com.healthcare.app.databinding.ItemRecordBinding;
import com.healthcare.app.model.MedicalRecord;

import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private final Context context;
    private List<MedicalRecord> records;

    public RecordAdapter(Context context, List<MedicalRecord> records) {
        this.context = context;
        this.records = records != null ? records : new ArrayList<>();
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecordBinding binding = ItemRecordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RecordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        holder.bind(records.get(position));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public void updateList(List<MedicalRecord> newRecords) {
        this.records = newRecords != null ? newRecords : new ArrayList<>();
        notifyDataSetChanged();
    }

    class RecordViewHolder extends RecyclerView.ViewHolder {

        private final ItemRecordBinding binding;

        RecordViewHolder(ItemRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MedicalRecord record) {
            binding.tvTitle.setText(record.getTitle());
            binding.tvDate.setText(record.getDate());
            binding.tvDoctor.setText(record.getDoctor());
            binding.tvHospital.setText(record.getHospital());
            binding.tvDetails.setText(record.getDetails());

            String type = record.getType() != null ? record.getType() : "";
            applyTypeStyle(type);
        }

        private void applyTypeStyle(String type) {
            int bgColor;
            int iconTint;
            int iconRes;
            String badgeLabel;

            switch (type.toLowerCase()) {
                case "prescription":
                    bgColor = ContextCompat.getColor(context, R.color.pastel_mint_20);
                    iconTint = ContextCompat.getColor(context, R.color.pastel_mint_dark);
                    iconRes = android.R.drawable.ic_menu_edit;
                    badgeLabel = "Prescription";
                    break;
                case "lab-result":
                    bgColor = ContextCompat.getColor(context, R.color.pastel_lavender_20);
                    iconTint = ContextCompat.getColor(context, R.color.pastel_lavender_dark);
                    iconRes = android.R.drawable.ic_menu_manage;
                    badgeLabel = "Lab Result";
                    break;
                case "note":
                    bgColor = ContextCompat.getColor(context, R.color.pastel_coral_20);
                    iconTint = ContextCompat.getColor(context, R.color.pastel_coral);
                    iconRes = android.R.drawable.ic_menu_info_details;
                    badgeLabel = "Note";
                    break;
                default:
                    bgColor = ContextCompat.getColor(context, R.color.pastel_blue_20);
                    iconTint = ContextCompat.getColor(context, R.color.pastel_blue_dark);
                    iconRes = android.R.drawable.ic_menu_my_calendar;
                    badgeLabel = "Appointment";
                    break;
            }

            GradientDrawable iconBg = new GradientDrawable();
            iconBg.setShape(GradientDrawable.RECTANGLE);
            iconBg.setCornerRadius(22f);
            iconBg.setColor(bgColor);
            binding.iconContainer.setBackground(iconBg);

            binding.imgTypeIcon.setImageResource(iconRes);
            binding.imgTypeIcon.setColorFilter(iconTint);

            binding.tvTypeBadge.setText(badgeLabel);
            binding.tvTypeBadge.setTextColor(iconTint);

            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setShape(GradientDrawable.RECTANGLE);
            badgeBg.setCornerRadius(20f);
            badgeBg.setColor(bgColor);
            binding.tvTypeBadge.setBackground(badgeBg);
        }
    }
}
