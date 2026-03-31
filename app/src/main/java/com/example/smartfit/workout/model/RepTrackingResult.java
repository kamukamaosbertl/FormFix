package com.example.smartfit.workout.model;

import androidx.annotation.NonNull;

import com.example.smartfit.workout.model.enums.RepPhase;

public class RepTrackingResult {

    private final boolean repCompleted;
    private final boolean formValid;
    private final boolean resetTracking;
    @NonNull
    private final RepPhase phase;
    @NonNull
    private final String message;

    public RepTrackingResult(
            boolean repCompleted,
            boolean formValid,
            boolean resetTracking,
            @NonNull RepPhase phase,
            @NonNull String message
    ) {
        this.repCompleted = repCompleted;
        this.formValid = formValid;
        this.resetTracking = resetTracking;
        this.phase = phase;
        this.message = message;
    }

    public boolean isRepCompleted() {
        return repCompleted;
    }

    public boolean isFormValid() {
        return formValid;
    }

    public boolean shouldResetTracking() {
        return resetTracking;
    }

    @NonNull
    public RepPhase getPhase() {
        return phase;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    public boolean isInProgress() {
        return phase == RepPhase.DESCENDING
                || phase == RepPhase.BOTTOM_REACHED
                || phase == RepPhase.ASCENDING;
    }

    public boolean isReady() {
        return phase == RepPhase.READY;
    }

    public boolean isInvalid() {
        return phase == RepPhase.INVALID;
    }

    public static RepTrackingResult ready(@NonNull String message) {
        return new RepTrackingResult(
                false,
                true,
                false,
                RepPhase.READY,
                message
        );
    }

    public static RepTrackingResult descending(boolean formValid, @NonNull String message) {
        return new RepTrackingResult(
                false,
                formValid,
                false,
                RepPhase.DESCENDING,
                message
        );
    }

    public static RepTrackingResult bottomReached(boolean formValid, @NonNull String message) {
        return new RepTrackingResult(
                false,
                formValid,
                false,
                RepPhase.BOTTOM_REACHED,
                message
        );
    }

    public static RepTrackingResult ascending(boolean formValid, @NonNull String message) {
        return new RepTrackingResult(
                false,
                formValid,
                false,
                RepPhase.ASCENDING,
                message
        );
    }

    public static RepTrackingResult completed(@NonNull String message) {
        return new RepTrackingResult(
                true,
                true,
                true,
                RepPhase.REP_COMPLETED,
                message
        );
    }

    public static RepTrackingResult invalid(boolean resetTracking, @NonNull String message) {
        return new RepTrackingResult(
                false,
                false,
                resetTracking,
                RepPhase.INVALID,
                message
        );
    }

    @NonNull
    @Override
    public String toString() {
        return "RepTrackingResult{" +
                "repCompleted=" + repCompleted +
                ", formValid=" + formValid +
                ", resetTracking=" + resetTracking +
                ", phase=" + phase +
                ", message='" + message + '\'' +
                '}';
    }
}