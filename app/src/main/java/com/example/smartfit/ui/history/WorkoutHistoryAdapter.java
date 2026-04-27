package com.example.smartfit.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfit.R;
import com.example.smartfit.workout.db.WorkoutSessionEntity;

import java.util.List;
import java.util.Locale;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder> {

    private final List<WorkoutSessionEntity> sessions;

    public WorkoutHistoryAdapter(List<WorkoutSessionEntity> sessions) {
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutSessionEntity session = sessions.get(position);

        holder.tvWorkoutType.setText(
                "Workout: " + formatWorkoutType(session.workoutType)
        );

        holder.tvRepCount.setText(
                "Reps: " + session.repCount
        );

        holder.tvDuration.setText(
                "Duration: " + formatDuration(session.activeDurationMillis)
        );

        holder.tvFeedback.setText(
                "Last feedback: " + session.lastFeedback
        );
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    private String formatWorkoutType(String rawType) {
        if (rawType == null) return "Unknown";
        return rawType.replace("_", " ");
    }

    private String formatDuration(long durationMillis) {
        long totalSeconds = durationMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvWorkoutType;
        TextView tvRepCount;
        TextView tvDuration;
        TextView tvFeedback;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvWorkoutType = itemView.findViewById(R.id.tv_history_workout_type);
            tvRepCount = itemView.findViewById(R.id.tv_history_rep_count);
            tvDuration = itemView.findViewById(R.id.tv_history_duration);
            tvFeedback = itemView.findViewById(R.id.tv_history_feedback);
        }
    }
}