package com.example.smartfit.workout.model.enums;

public enum FormBreakAction {
    PAUSE_SESSION("Pause Session", false),
    CANCEL_SESSION("Cancel Session", true);

    private final String displayName;
    private final boolean terminal;

    FormBreakAction(String displayName, boolean terminal) {
        this.displayName = displayName;
        this.terminal = terminal;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isTerminal() {
        return terminal;
    }
}