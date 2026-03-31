package com.example.smartfit.workout.model;

import com.example.smartfit.workout.model.enums.FeedbackLevel;

public class WorkoutFeedback {

    private final boolean correctForm;
    private final String message;
    private final FeedbackLevel level;

    public WorkoutFeedback(boolean correctForm, String message, FeedbackLevel level) {
        this.correctForm = correctForm;
        this.message = message;
        this.level = level;
    }

    public boolean isCorrectForm() {
        return correctForm;
    }

    public String getMessage() {
        return message;
    }

    public FeedbackLevel getLevel() {
        return level;
    }

    public boolean shouldSpeakByDefault() {
        return level != null && level.shouldSpeakByDefault();
    }

    public static WorkoutFeedback good(String message) {
        return new WorkoutFeedback(true, message, FeedbackLevel.GOOD);
    }

    public static WorkoutFeedback info(String message) {
        return new WorkoutFeedback(true, message, FeedbackLevel.INFO);
    }

    public static WorkoutFeedback warning(String message) {
        return new WorkoutFeedback(false, message, FeedbackLevel.WARNING);
    }

    public static WorkoutFeedback error(String message) {
        return new WorkoutFeedback(false, message, FeedbackLevel.ERROR);
    }
}