package com.example.universityemergencyapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    public interface OnAlertClickListener {
        void onAlertClick(Alert alert);
    }

    private final List<Alert> alerts = new ArrayList<>();
    private final OnAlertClickListener listener;

    public AlertAdapter(List<Alert> initialAlerts, OnAlertClickListener listener) {
        this.listener = listener;
        if (initialAlerts != null) {
            this.alerts.addAll(initialAlerts);
        }
    }

    public void submitList(List<Alert> newAlerts) {
        alerts.clear();
        if (newAlerts != null) {
            alerts.addAll(newAlerts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alerts.get(position);
        holder.tvTitle.setText(alert.getTitle());
        holder.tvDesc.setText(alert.getDescription());
        holder.tvTime.setText(alert.getTime());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAlertClick(alert);
        });
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvTime;

        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvDesc = itemView.findViewById(R.id.tvAlertDesc);
            tvTime = itemView.findViewById(R.id.tvAlertTime);
        }
    }
}
