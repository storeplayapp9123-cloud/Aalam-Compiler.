package com.aalamstudio.compiler;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BuildLogAdapter extends RecyclerView.Adapter<BuildLogAdapter.LogViewHolder> {

    public static final int TYPE_DONE = 0;      // green check, e.g. "Project loaded successfully"
    public static final int TYPE_IN_PROGRESS = 1; // cyan spinner/circle, e.g. "Processing assets... (78%)"
    public static final int TYPE_SUB_STEP = 2;   // dim bullet, indented, e.g. "Optimizing images..."

    public static class LogEntry {
        public String timestamp; // "10:24:10" or null for sub-steps
        public String text;
        public int type;

        public LogEntry(String timestamp, String text, int type) {
            this.timestamp = timestamp;
            this.text = text;
            this.type = type;
        }
    }

    private final List<LogEntry> entries;

    public BuildLogAdapter(List<LogEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_build_log, parent, false);
        return new LogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry entry = entries.get(position);

        if (entry.type == TYPE_SUB_STEP) {
            holder.tvTimestamp.setVisibility(View.GONE);
            holder.tvIcon.setText("\u00B7"); // ·
            holder.tvIcon.setTextColor(Color.parseColor("#5E5E66"));
            holder.tvText.setText(entry.text);
            holder.tvText.setTextColor(Color.parseColor("#5E5E66"));
            holder.itemView.setPadding(dp(holder, 32), dp(holder, 2), 0, dp(holder, 2));
        } else {
            holder.tvTimestamp.setVisibility(View.VISIBLE);
            holder.tvTimestamp.setText(entry.timestamp);
            holder.itemView.setPadding(dp(holder, 0), dp(holder, 3), 0, dp(holder, 3));

            if (entry.type == TYPE_DONE) {
                holder.tvIcon.setText("\u2713"); // ✓
                holder.tvIcon.setTextColor(Color.parseColor("#3DD68C"));
                holder.tvText.setTextColor(Color.parseColor("#C9C9CE"));
            } else { // IN_PROGRESS
                holder.tvIcon.setText("\u25CB"); // ○
                holder.tvIcon.setTextColor(Color.parseColor("#3DC6D6"));
                holder.tvText.setTextColor(Color.parseColor("#3DC6D6"));
            }
            holder.tvText.setText(entry.text);
        }
    }

    private int dp(RecyclerView.ViewHolder h, int value) {
        float density = h.itemView.getResources().getDisplayMetrics().density;
        return (int) (value * density);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp, tvIcon, tvText;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvText = itemView.findViewById(R.id.tvText);
        }
    }
}
