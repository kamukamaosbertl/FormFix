package com.example.smartfit.workout.model;

import androidx.annotation.NonNull;

import com.example.smartfit.workout.model.enums.WorkoutState;
import com.example.smartfit.workout.model.enums.WorkoutType;

public class WorkoutSession {

    private final long sessionId;
    @NonNull
    private final WorkoutType workoutType;
    private final long startTimeMillis;

    private long endTimeMillis;
    private long pausedAtMillis;
    private long totalPausedDurationMillis;

    @NonNull
    private WorkoutState state;
    @NonNull
    private String lastFeedback;

    @NonNull
    private final WorkoutMetrics metrics;

    private final long targetDurationMillis;

    public WorkoutSession(
            long sessionId,
            @NonNull WorkoutType workoutType,
            long startTimeMillis,
            long targetDurationMillis
    ) {
        this.sessionId = sessionId;
        this.workoutType = workoutType;
        this.startTimeMillis = startTimeMillis;
        this.targetDurationMillis = Math.max(targetDurationMillis, 0L);

        this.endTimeMillis = 0L;
        this.pausedAtMillis = 0L;
        this.totalPausedDurationMillis = 0L;

        this.state = WorkoutState.STARTED;
        this.lastFeedback = "Session started";
        this.metrics = new WorkoutMetrics();

        if (workoutType.isDurationBased()) {
            this.metrics.setRemainingDurationMillis(this.targetDurationMillis);
        }
    }

    public long getSessionId() {
        return sessionId;
    }

    @NonNull
    public WorkoutType getWorkoutType() {
        return workoutType;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public long getTargetDurationMillis() {
        return targetDurationMillis;
    }

    @NonNull
    public WorkoutState getState() {
        return state;
    }

    public void setState(@NonNull WorkoutState state) {
        this.state = state;
    }

    @NonNull
    public String getLastFeedback() {
        return lastFeedback;
    }

    public void setLastFeedback(@NonNull String lastFeedback) {
        this.lastFeedback = lastFeedback;
    }

    @NonNull
    public WorkoutMetrics getMetrics() {
        return metrics;
    }

    public boolean isActive() {
        return state.isActive();
    }

    public boolean isPaused() {
        return state.isPaused();
    }

    public boolean isFinished() {
        return state.isFinished();
    }

    public boolean isRepBased() {
        return workoutType.isRepBased();
    }

    public boolean isDurationBased() {
        return workoutType.isDurationBased();
    }

    public int getRepCount() {
        return metrics.getRepCount();
    }

    public void incrementRepCount() {
        metrics.incrementRepCount();
    }

    public long getHoldDurationMillis() {
        return metrics.getHoldDurationMillis();
    }

    public void setHoldDurationMillis(long holdDurationMillis) {
        metrics.setHoldDurationMillis(holdDurationMillis);
    }

    public long getRemainingDurationMillis() {
        return metrics.getRemainingDurationMillis();
    }

    public void setRemainingDurationMillis(long remainingDurationMillis) {
        metrics.setRemainingDurationMillis(remainingDurationMillis);
    }

    public void reduceRemainingDurationMillis(long deltaMillis) {
        if (deltaMillis <= 0L) {
            return;
        }

        long currentRemaining = metrics.getRemainingDurationMillis();
        metrics.setRemainingDurationMillis(Math.max(0L, currentRemaining - deltaMillis));
    }

    public boolean isDurationGoalReached() {
        return isDurationBased() && metrics.getRemainingDurationMillis() <= 0L;
    }

    public void pause(long timestampMillis) {
        if (state != WorkoutState.STARTED) {
            return;
        }

        pausedAtMillis = timestampMillis;
        state = WorkoutState.PAUSED;
    }

    public void resume(long timestampMillis) {
        if (state != WorkoutState.PAUSED) {
            return;
        }

        totalPausedDurationMillis += Math.max(0L, timestampMillis - pausedAtMillis);
        pausedAtMillis = 0L;
        state = WorkoutState.STARTED;
    }

    public void complete(long timestampMillis) {
        finalizePausedTimeIfNeeded(timestampMillis);
        endTimeMillis = timestampMillis;
        state = WorkoutState.COMPLETED;
        metrics.setActiveDurationMillis(getDurationMillis());
    }

    public void cancel(long timestampMillis) {
        finalizePausedTimeIfNeeded(timestampMillis);
        endTimeMillis = timestampMillis;
        state = WorkoutState.CANCELLED;
        metrics.setActiveDurationMillis(getDurationMillis());
    }

    public long getDurationMillis() {
        long effectiveEnd;

        if (state == WorkoutState.PAUSED && pausedAtMillis != 0L) {
            effectiveEnd = pausedAtMillis;
        } else if (endTimeMillis != 0L) {
            effectiveEnd = endTimeMillis;
        } else {
            effectiveEnd = System.currentTimeMillis();
        }

        return Math.max(0L, effectiveEnd - startTimeMillis - totalPausedDurationMillis);
    }

    private void finalizePausedTimeIfNeeded(long timestampMillis) {
        if (state == WorkoutState.PAUSED && pausedAtMillis != 0L) {
            totalPausedDurationMillis += Math.max(0L, timestampMillis - pausedAtMillis);
            pausedAtMillis = 0L;
        }
    }
}