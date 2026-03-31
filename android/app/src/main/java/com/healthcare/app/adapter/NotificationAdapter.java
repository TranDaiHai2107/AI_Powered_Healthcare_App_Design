package com.healthcare.app.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.healthcare.app.R;
import com.healthcare.app.databinding.ItemNotificationBinding;
import com.healthcare.app.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private List<Notification> notifications;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications != null ? notifications : new ArrayList<>();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateList(List<Notification> newNotifications) {
        this.notifications = newNotifications != null ? newNotifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    public long getUnreadCount() {
        long count = 0;
        for (Notification n : notifications) {
            if (n.getIsRead() == null || !n.getIsRead()) {
                count++;
            }
        }
        return count;
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final ItemNotificationBinding binding;

        NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Notification notification) {
            boolean isUnread = notification.getIsRead() == null || !notification.getIsRead();

            binding.tvTitle.setText(notification.getTitle());
            binding.tvTitle.setTypeface(null, isUnread ? Typeface.BOLD : Typeface.NORMAL);
            binding.tvMessage.setText(notification.getMessage());
            binding.tvTime.setText(notification.getTime());

            binding.viewUnreadDot.setVisibility(isUnread ? View.VISIBLE : View.GONE);
            binding.viewUnreadBorder.setVisibility(isUnread ? View.VISIBLE : View.GONE);

            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(ContextCompat.getColor(context, R.color.pastel_blue));
            binding.viewUnreadDot.setBackground(dotBg);

            String type = notification.getType() != null ? notification.getType() : "";
            applyIconStyle(type);
        }

        private void applyIconStyle(String type) {
            int bgColor;
            int iconTint;
            int iconRes;

            switch (type.toLowerCase()) {
                case "appointment":
                    bgColor = ContextCompat.getColor(context, R.color.pastel_blue_20);
                    iconTint = ContextCompat.getColor(context, R.color.pastel_blue_dark);
                    iconRes = android.R.drawable.ic_menu_my_calendar;
                    break;
                case "prescription":
                    bgColor = ContextCompat.getColor(context, R.color.pastel_mint_20);
                    iconTint = ContextCompat.getColor(context, R.color.pastel_mint_dark);
                    iconRes = android.R.drawable.ic_menu_edit;
                    break;
                case "lab":
                    bgColor = ContextCompat.getColor(context, R.color.pastel_lavender_20);
                    iconTint = ContextCompat.getColor(context, R.color.pastel_lavender_dark);
                    iconRes = android.R.drawable.ic_menu_manage;
                    break;
                case "payment":
                    bgColor = ContextCompat.getColor(context, R.color.pastel_coral_20);
                    iconTint = ContextCompat.getColor(context, R.color.pastel_coral);
                    iconRes = android.R.drawable.ic_menu_info_details;
                    break;
                default:
                    bgColor = ContextCompat.getColor(context, R.color.pastel_blue_20);
                    iconTint = ContextCompat.getColor(context, R.color.pastel_blue_dark);
                    iconRes = android.R.drawable.ic_popup_reminder;
                    break;
            }

            GradientDrawable iconBg = new GradientDrawable();
            iconBg.setShape(GradientDrawable.OVAL);
            iconBg.setColor(bgColor);
            binding.iconContainer.setBackground(iconBg);

            binding.imgIcon.setImageResource(iconRes);
            binding.imgIcon.setColorFilter(iconTint);
        }
    }
}
