package com.example.smartfit.workout.model.enums;

public enum FeedbackLevel {
    GOOD("Good", false),
    INFO("Info", true),
    WARNING("Warning", true),
    ERROR("Error", true);

    private final String displayName;
    private final boolean speakByDefault;

    FeedbackLevel(String displayName, boolean speakByDefault) {
        this.displayName = displayName;
        this.speakByDefault = speakByDefault;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean shouldSpeakByDefault() {
        return speakByDefault;
    }
}