package com.healthcare.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityOnboardingBinding;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;

    private final int[] titles = {
            R.string.onboarding_title_1,
            R.string.onboarding_title_2,
            R.string.onboarding_title_3
    };

    private final int[] descriptions = {
            R.string.onboarding_desc_1,
            R.string.onboarding_desc_2,
            R.string.onboarding_desc_3
    };

    private final int[] icons = {
            android.R.drawable.ic_menu_search,
            android.R.drawable.ic_menu_my_calendar,
            android.R.drawable.ic_menu_agenda
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
        setupDots(0);

        binding.btnSkip.setOnClickListener(v -> navigateToLogin());

        binding.btnNext.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current < titles.length - 1) {
                binding.viewPager.setCurrentItem(current + 1);
            } else {
                navigateToLogin();
            }
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                if (position == titles.length - 1) {
                    binding.btnNext.setText(R.string.get_started);
                    binding.btnSkip.setVisibility(View.INVISIBLE);
                } else {
                    binding.btnNext.setText(R.string.next);
                    binding.btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupViewPager() {
        binding.viewPager.setAdapter(new RecyclerView.Adapter<SlideViewHolder>() {
            @NonNull
            @Override
            public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_onboarding_slide, parent, false);
                return new SlideViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
                holder.title.setText(titles[position]);
                holder.description.setText(descriptions[position]);
                holder.icon.setImageResource(icons[position]);
            }

            @Override
            public int getItemCount() {
                return titles.length;
            }
        });
    }

    private void setupDots(int currentPosition) {
        binding.dotsLayout.removeAllViews();
        for (int i = 0; i < titles.length; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params;
            if (i == currentPosition) {
                params = new LinearLayout.LayoutParams(32, 8);
                dot.setBackgroundResource(R.drawable.bg_dot_active);
            } else {
                params = new LinearLayout.LayoutParams(8, 8);
                dot.setBackgroundResource(R.drawable.bg_dot_inactive);
            }
            float density = getResources().getDisplayMetrics().density;
            if (i == currentPosition) {
                params.width = (int) (32 * density);
                params.height = (int) (8 * density);
            } else {
                params.width = (int) (8 * density);
                params.height = (int) (8 * density);
            }
            params.setMargins((int) (4 * density), 0, (int) (4 * density), 0);
            dot.setLayoutParams(params);
            binding.dotsLayout.addView(dot);
        }
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView icon;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvSlideTitle);
            description = itemView.findViewById(R.id.tvSlideDescription);
            icon = itemView.findViewById(R.id.ivSlideIcon);
        }
    }
}
