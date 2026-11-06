package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * CategoryAdapter binds marketplace categories into horizontal filter chips.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategorySelectedListener {
        void onCategorySelected(Category category, int position);
    }

    private final List<Category> categories = new ArrayList<>();
    private final OnCategorySelectedListener listener;
    private int selectedPosition = 0;

    public CategoryAdapter(List<Category> categories, OnCategorySelectedListener listener) {
        if (categories != null) {
            this.categories.addAll(categories);
        }
        this.listener = listener;
    }

    public void setSelectedCategory(String categoryName) {
        for (int i = 0; i < categories.size(); i++) {
            boolean matches = categories.get(i).getName().equalsIgnoreCase(categoryName);
            categories.get(i).setSelected(matches);
            if (matches) {
                int oldPos = selectedPosition;
                selectedPosition = i;
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
            }
        }
    }

    public String getSelectedCategoryName() {
        if (selectedPosition >= 0 && selectedPosition < categories.size()) {
            return categories.get(selectedPosition).getName();
        }
        return SampleDataProvider.CATEGORY_ALL;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_chip, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout layoutCategoryChip;
        private final ImageView ivCategoryIcon;
        private final TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutCategoryChip = itemView.findViewById(R.id.layoutCategoryChip);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos != selectedPosition) {
                    categories.get(selectedPosition).setSelected(false);
                    notifyItemChanged(selectedPosition);

                    selectedPosition = pos;
                    categories.get(selectedPosition).setSelected(true);
                    notifyItemChanged(selectedPosition);

                    if (listener != null) {
                        listener.onCategorySelected(categories.get(selectedPosition), selectedPosition);
                    }
                }
            });
        }

        public void bind(Category category, boolean isSelected) {
            tvCategoryName.setText(category.getName());
            ivCategoryIcon.setImageResource(category.getIconResId());
            ivCategoryIcon.setContentDescription(category.getName() + " category filter");

            if (isSelected) {
                layoutCategoryChip.setBackgroundResource(R.drawable.bg_category_chip_selected);
                tvCategoryName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                ivCategoryIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else {
                layoutCategoryChip.setBackgroundResource(R.drawable.bg_category_chip_unselected);
                tvCategoryName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                ivCategoryIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.eco_green_primary));
            }
        }
    }
}
