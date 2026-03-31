package com.example.smartfit.workout.model.enums;

public enum RepPhase {
    READY("Ready"),
    DESCENDING("Lowering"),
    BOTTOM_REACHED("Bottom Reached"),
    ASCENDING("Rising"),
    REP_COMPLETED("Rep Completed"),
    INVALID("Invalid Form");

    private final String displayName;

    RepPhase(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}