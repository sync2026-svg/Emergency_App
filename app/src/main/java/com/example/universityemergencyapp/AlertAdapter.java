package com.example.universityemergencyapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    public interface OnAlertClickListener {
        void onAlertClick(Alert alert);
    }

    public interface OnAlertDeleteListener {
        void onAlertDelete(Alert alert, int position);
    }

    private final List<Alert> alerts = new ArrayList<>();
    private final OnAlertClickListener listener;
    private OnAlertDeleteListener deleteListener;

    public AlertAdapter(List<Alert> initialAlerts, OnAlertClickListener listener) {
        this.listener = listener;
        if (initialAlerts != null) {
            this.alerts.addAll(initialAlerts);
        }
    }

    public void setOnAlertDeleteListener(OnAlertDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void submitList(List<Alert> newAlerts) {
        alerts.clear();
        if (newAlerts != null) {
            alerts.addAll(newAlerts);
        }
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        if (position >= 0 && position < alerts.size()) {
            alerts.remove(position);
            notifyItemRemoved(position);
        }
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

        if (deleteListener != null && holder.btnDeleteAlert != null) {
            holder.btnDeleteAlert.setVisibility(View.VISIBLE);
            holder.btnDeleteAlert.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    deleteListener.onAlertDelete(alert, pos);
                }
            });
        } else if (holder.btnDeleteAlert != null) {
            holder.btnDeleteAlert.setVisibility(View.GONE);
        }

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
        ImageButton btnDeleteAlert;

        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvDesc = itemView.findViewById(R.id.tvAlertDesc);
            tvTime = itemView.findViewById(R.id.tvAlertTime);
            btnDeleteAlert = itemView.findViewById(R.id.btnDeleteAlert);
        }
    }
}
