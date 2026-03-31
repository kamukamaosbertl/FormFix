package com.example.smartfit.workout.engine;

import androidx.annotation.NonNull;

import com.example.smartfit.workout.model.PoseFrameData;
import com.example.smartfit.workout.model.WorkoutFeedback;
import com.example.smartfit.workout.model.enums.WorkoutType;

public class WorkoutFeedbackEngine {

    // =========================================================
    // PUBLIC ENTRY POINT
    // =========================================================
    @NonNull
    public WorkoutFeedback analyze(
            @NonNull WorkoutType workoutType,
            @NonNull PoseFrameData frameData
    ) {
        switch (workoutType) {
            case PUSH_UP:
                return analyzePushUp(frameData);

            case SQUAT:
                return analyzeSquat(frameData);

            case SIT_UP:
                return analyzeSitUp(frameData);

            case PLANK:
                return analyzePlank(frameData);

            default:
                return WorkoutFeedback.warning("Workout type not supported yet");
        }
    }

    // =========================================================
    // PUSH-UP FEEDBACK
    // =========================================================
    @NonNull
    private WorkoutFeedback analyzePushUp(@NonNull PoseFrameData frameData) {
        double elbowAngle = frameData.getAverageElbowAngle();
        double backAngle = frameData.getBackAngle();

        // Main form issue first
        if (backAngle < 150.0) {
            return WorkoutFeedback.warning("Keep your back straighter");
        }

        // Top position
        if (elbowAngle >= 150.0) {
            return WorkoutFeedback.good("Strong push-up start position");
        }

        // Mid movement
        if (elbowAngle > 90.0) {
            return WorkoutFeedback.good("Lower with control");
        }

        // Bottom position
        return WorkoutFeedback.good("Good push-up depth");
    }

    // =========================================================
    // SQUAT FEEDBACK
    // Objective 4 focus:
    // detect bad squat form from hip-knee-ankle chain and posture
    // =========================================================
    @NonNull
    private WorkoutFeedback analyzeSquat(@NonNull PoseFrameData frameData) {
        double kneeAngle = frameData.getAverageKneeAngle();
        double hipAngle = frameData.getAverageHipAngle();
        double backAngle = frameData.getBackAngle();

        // -----------------------------------------------------
        // Highest-priority errors first
        // -----------------------------------------------------

        // Too much torso collapse / forward lean
        if (backAngle < 135.0) {
            return WorkoutFeedback.warning("Your back is rounding. Keep your chest up");
        }

        // Deep squat but hip/back posture still not ideal
        if (hipAngle < 70.0 && backAngle < 145.0) {
            return WorkoutFeedback.warning("Stay tall through the squat");
        }

        // -----------------------------------------------------
        // Movement coaching
        // -----------------------------------------------------

        // User still standing or barely started
        if (kneeAngle >= 160.0) {
            return WorkoutFeedback.info("Start lowering into the squat");
        }

        // Going down but not yet deep enough
        if (kneeAngle > 110.0) {
            return WorkoutFeedback.good("Lower a little more");
        }

        // Good squat range
        if (kneeAngle > 80.0) {
            return WorkoutFeedback.good("Good squat depth");
        }

        // Very deep position - still okay if posture is controlled
        return WorkoutFeedback.good("Deep squat, keep it controlled");
    }

    // =========================================================
    // SIT-UP FEEDBACK
    // =========================================================
    @NonNull
    private WorkoutFeedback analyzeSitUp(@NonNull PoseFrameData frameData) {
        double hipAngle = frameData.getAverageHipAngle();

        if (hipAngle >= 140.0) {
            return WorkoutFeedback.info("Lift your upper body");
        }

        if (hipAngle > 95.0) {
            return WorkoutFeedback.good("Keep going");
        }

        return WorkoutFeedback.good("Good sit-up height");
    }

    // =========================================================
    // PLANK FEEDBACK
    // =========================================================
    @NonNull
    private WorkoutFeedback analyzePlank(@NonNull PoseFrameData frameData) {
        double backAngle = frameData.getBackAngle();
        double elbowAngle = frameData.getAverageElbowAngle();

        if (backAngle < 160.0) {
            return WorkoutFeedback.warning("Keep your body straighter");
        }

        if (elbowAngle < 60.0 || elbowAngle > 140.0) {
            return WorkoutFeedback.warning("Adjust your arm position");
        }

        return WorkoutFeedback.good("Strong plank hold");
    }
}