package com.example.ghor_cycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for presenting recycling centers and community drop-off points.
 */
public class RecyclingCenterAdapter extends RecyclerView.Adapter<RecyclingCenterAdapter.CenterViewHolder> {

    public interface OnCenterActionListener {
        void onGetDirections(RecyclingCenter center);
        void onContactCenter(RecyclingCenter center);
    }

    private final List<RecyclingCenter> centers = new ArrayList<>();
    private final OnCenterActionListener actionListener;

    public RecyclingCenterAdapter(List<RecyclingCenter> initialList, OnCenterActionListener listener) {
        if (initialList != null) {
            this.centers.addAll(initialList);
        }
        this.actionListener = listener;
    }

    public void updateCenters(List<RecyclingCenter> newList) {
        this.centers.clear();
        if (newList != null) {
            this.centers.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CenterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recycling_center, parent, false);
        return new CenterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CenterViewHolder holder, int position) {
        RecyclingCenter center = centers.get(position);
        holder.bind(center, actionListener);
    }

    @Override
    public int getItemCount() {
        return centers.size();
    }

    public static class CenterViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final TextView tvSampleBadge;
        private final TextView tvAddress;
        private final TextView tvMaterials;
        private final TextView tvHours;
        private final TextView tvNotes;
        private final Button btnDirections;
        private final Button btnCall;

        public CenterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCenterName);
            tvSampleBadge = itemView.findViewById(R.id.tvCenterSampleBadge);
            tvAddress = itemView.findViewById(R.id.tvCenterAddress);
            tvMaterials = itemView.findViewById(R.id.tvCenterMaterials);
            tvHours = itemView.findViewById(R.id.tvCenterHours);
            tvNotes = itemView.findViewById(R.id.tvCenterNotes);
            btnDirections = itemView.findViewById(R.id.btnCenterDirections);
            btnCall = itemView.findViewById(R.id.btnCenterCall);
        }

        public void bind(final RecyclingCenter center, final OnCenterActionListener listener) {
            tvName.setText(center.getName());
            tvAddress.setText(center.getAddressOrArea());
            tvMaterials.setText("Accepted: " + center.getFormattedMaterials());
            tvHours.setText("Hours: " + center.getOpeningHours());
            tvNotes.setText(center.getNotes());

            tvSampleBadge.setVisibility(center.isSampleData() ? View.VISIBLE : View.GONE);

            // Directions button: only enabled when valid coordinates exist
            btnDirections.setEnabled(center.hasValidCoordinates());
            btnDirections.setOnClickListener(v -> {
                if (listener != null) listener.onGetDirections(center);
            });

            btnCall.setOnClickListener(v -> {
                if (listener != null) listener.onContactCenter(center);
            });
        }
    }
}
