package com.example.smartfit.workout.model.enums;

public enum VoiceFeedbackMode {
    OFF("Off", false, false),
    IMPORTANT_ONLY("Important Only", true, false),
    ALL("All Feedback", true, true);

    private final String displayName;
    private final boolean enabled;
    private final boolean includesPositiveFeedback;

    VoiceFeedbackMode(String displayName, boolean enabled, boolean includesPositiveFeedback) {
        this.displayName = displayName;
        this.enabled = enabled;
        this.includesPositiveFeedback = includesPositiveFeedback;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean includesPositiveFeedback() {
        return includesPositiveFeedback;
    }
}