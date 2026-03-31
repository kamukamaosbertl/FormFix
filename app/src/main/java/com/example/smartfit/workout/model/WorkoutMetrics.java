package com.example.smartfit.workout.model;

public class WorkoutMetrics {

    private int repCount;
    private long activeDurationMillis;
    private long holdDurationMillis;
    private long remainingDurationMillis;

    public WorkoutMetrics() {
        this.repCount = 0;
        this.activeDurationMillis = 0L;
        this.holdDurationMillis = 0L;
        this.remainingDurationMillis = 0L;
    }

    public int getRepCount() {
        return repCount;
    }

    public long getActiveDurationMillis() {
        return activeDurationMillis;
    }

    public long getHoldDurationMillis() {
        return holdDurationMillis;
    }

    public long getRemainingDurationMillis() {
        return remainingDurationMillis;
    }

    public void incrementRepCount() {
        repCount++;
    }

    public void setActiveDurationMillis(long activeDurationMillis) {
        this.activeDurationMillis = Math.max(activeDurationMillis, 0L);
    }

    public void setHoldDurationMillis(long holdDurationMillis) {
        this.holdDurationMillis = Math.max(holdDurationMillis, 0L);
    }

    public void setRemainingDurationMillis(long remainingDurationMillis) {
        this.remainingDurationMillis = Math.max(remainingDurationMillis, 0L);
    }

    public void reset() {
        repCount = 0;
        activeDurationMillis = 0L;
        holdDurationMillis = 0L;
        remainingDurationMillis = 0L;
    }
}