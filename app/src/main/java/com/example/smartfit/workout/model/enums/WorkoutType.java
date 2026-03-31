package com.example.smartfit.workout.model.enums;

public enum WorkoutType {
    PUSH_UP(
            "Push Up",
            true,
            false,
            0L,
            "Use full body alignment and controlled elbow movement."
    ),
    SQUAT(
            "Squat",
            true,
            false,
            0L,
            "Keep your chest up and lower with control."
    ),
    SIT_UP(
            "Sit Up",
            true,
            false,
            0L,
            "Lift with control and avoid rushing the motion."
    ),
    PLANK(
            "Plank",
            false,
            true,
            30000L,
            "Hold a straight body line and keep your core engaged."
    );

    private final String displayName;
    private final boolean repBased;
    private final boolean durationBased;
    private final long defaultTargetDurationMillis;
    private final String defaultInstruction;

    WorkoutType(
            String displayName,
            boolean repBased,
            boolean durationBased,
            long defaultTargetDurationMillis,
            String defaultInstruction
    ) {
        this.displayName = displayName;
        this.repBased = repBased;
        this.durationBased = durationBased;
        this.defaultTargetDurationMillis = defaultTargetDurationMillis;
        this.defaultInstruction = defaultInstruction;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRepBased() {
        return repBased;
    }

    public boolean isDurationBased() {
        return durationBased;
    }

    public long getDefaultTargetDurationMillis() {
        return defaultTargetDurationMillis;
    }

    public String getDefaultInstruction() {
        return defaultInstruction;
    }
}