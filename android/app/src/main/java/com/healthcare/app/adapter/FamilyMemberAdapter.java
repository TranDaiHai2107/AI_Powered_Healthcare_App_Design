package com.healthcare.app.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.healthcare.app.R;
import com.healthcare.app.databinding.ItemFamilyMemberBinding;
import com.healthcare.app.model.FamilyMember;

import java.util.ArrayList;
import java.util.List;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.FamilyMemberViewHolder> {

    private final Context context;
    private List<FamilyMember> members;
    private final OnFamilyMemberClickListener listener;

    public interface OnFamilyMemberClickListener {
        void onEdit(FamilyMember member);
        void onDelete(FamilyMember member);
    }

    public FamilyMemberAdapter(Context context, List<FamilyMember> members, OnFamilyMemberClickListener listener) {
        this.context = context;
        this.members = members != null ? members : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public FamilyMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFamilyMemberBinding binding = ItemFamilyMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FamilyMemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyMemberViewHolder holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void updateList(List<FamilyMember> newMembers) {
        this.members = newMembers != null ? newMembers : new ArrayList<>();
        notifyDataSetChanged();
    }

    class FamilyMemberViewHolder extends RecyclerView.ViewHolder {
        private final ItemFamilyMemberBinding binding;

        FamilyMemberViewHolder(ItemFamilyMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FamilyMember member) {
            binding.tvName.setText(member.getName());
            
            String initial = "";
            if (member.getName() != null && !member.getName().isEmpty()) {
                initial = member.getName().substring(0, 1).toUpperCase();
            }
            binding.tvAvatar.setText(initial);

            String rel = member.getRelationship() != null ? member.getRelationship() : "Other";
            binding.tvRelationship.setText(rel);

            String dob = member.getDateOfBirth() != null ? member.getDateOfBirth() : "N/A";
            binding.tvDob.setText("DOB: " + dob);

            applyRelationshipBadge(rel);

            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(member);
            });
            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(member);
            });
        }

        private void applyRelationshipBadge(String relationship) {
            int bgColor, textColor;
            switch (relationship.toLowerCase()) {
                case "self":
                    bgColor = ContextCompat.getColor(context, R.color.pastel_blue_20);
                    textColor = ContextCompat.getColor(context, R.color.pastel_blue_dark);
                    break;
                case "parent":
                    bgColor = ContextCompat.getColor(context, R.color.pastel_mint_20);
                    textColor = ContextCompat.getColor(context, R.color.pastel_mint_dark);
                    break;
                case "child":
                    bgColor = ContextCompat.getColor(context, R.color.pastel_orange_20);
                    textColor = ContextCompat.getColor(context, R.color.pastel_orange_dark);
                    break;
                case "spouse":
                    bgColor = ContextCompat.getColor(context, R.color.pastel_lavender);
                    textColor = ContextCompat.getColor(context, R.color.healthcare_dark);
                    break;
                default:
                    bgColor = ContextCompat.getColor(context, R.color.gray_100);
                    textColor = ContextCompat.getColor(context, R.color.healthcare_muted);
                    break;
            }
            binding.tvRelationship.setTextColor(textColor);
            GradientDrawable badge = new GradientDrawable();
            badge.setShape(GradientDrawable.RECTANGLE);
            badge.setCornerRadius(20f);
            badge.setColor(bgColor);
            binding.tvRelationship.setBackground(badge);
            
            GradientDrawable avatarBg = new GradientDrawable();
            avatarBg.setShape(GradientDrawable.OVAL);
            avatarBg.setColor(bgColor);
            binding.tvAvatar.setBackground(avatarBg);
            binding.tvAvatar.setTextColor(textColor);
        }
    }
}
