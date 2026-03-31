package com.example.smartfit.workout.model.enums;

public enum WorkoutState {
    IDLE("Idle", false, false),
    STARTED("Running", true, false),
    PAUSED("Paused", false, true),
    COMPLETED("Completed", false, false),
    CANCELLED("Cancelled", false, false);

    private final String displayName;
    private final boolean active;
    private final boolean paused;

    WorkoutState(String displayName, boolean active, boolean paused) {
        this.displayName = displayName;
        this.active = active;
        this.paused = paused;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED;
    }
}