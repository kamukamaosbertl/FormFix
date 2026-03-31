package com.example.smartfit.workout.engine;

import androidx.annotation.NonNull;

import com.example.smartfit.workout.model.PoseFrameData;
import com.example.smartfit.workout.model.RepTrackingResult;
import com.example.smartfit.workout.model.enums.RepPhase;
import com.example.smartfit.workout.model.enums.WorkoutType;

public class PoseAnalyzer {

    private boolean repInProgress = false;
    private boolean reachedBottom = false;
    private boolean formWasValidThroughoutRep = true;

    @NonNull
    public RepTrackingResult analyzeRep(
            @NonNull WorkoutType workoutType,
            @NonNull PoseFrameData frameData
    ) {
        switch (workoutType) {
            case PUSH_UP:
                return analyzePushUpRep(frameData);

            case SQUAT:
                return analyzeSquatRep(frameData);

            case SIT_UP:
                return analyzeSitUpRep(frameData);

            default:
                return RepTrackingResult.invalid(
                        false,
                        "Rep analysis not supported for " + workoutType.getDisplayName()
                );
        }
    }

    @NonNull
    private RepTrackingResult analyzePushUpRep(@NonNull PoseFrameData frameData) {
        double elbowAngle = frameData.getAverageElbowAngle();
        double backAngle = frameData.getBackAngle();

        boolean validTopPosition = elbowAngle >= 150.0;
        boolean validBottomPosition = elbowAngle <= 90.0;
        boolean validForm = backAngle >= 150.0;

        if (!repInProgress) {
            if (validTopPosition && validForm) {
                repInProgress = true;
                reachedBottom = false;
                formWasValidThroughoutRep = true;
                return RepTrackingResult.ready("Push-up ready");
            }

            return RepTrackingResult.invalid(
                    false,
                    "Hold a straight push-up start position"
            );
        }

        if (!validForm) {
            formWasValidThroughoutRep = false;
        }

        if (!reachedBottom) {
            if (validBottomPosition) {
                reachedBottom = true;
                return RepTrackingResult.bottomReached(
                        formWasValidThroughoutRep,
                        "Push-up depth reached"
                );
            }

            return RepTrackingResult.descending(
                    formWasValidThroughoutRep,
                    "Lower with control"
            );
        }

        if (validTopPosition) {
            boolean validRep = formWasValidThroughoutRep && validForm;
            reset();

            if (validRep) {
                return RepTrackingResult.completed("Push-up rep counted");
            }

            return RepTrackingResult.invalid(
                    true,
                    "Rep not counted. Keep your back straight"
            );
        }

        return RepTrackingResult.ascending(
                formWasValidThroughoutRep,
                "Push back up"
        );
    }

    @NonNull
    private RepTrackingResult analyzeSquatRep(@NonNull PoseFrameData frameData) {
        double kneeAngle = frameData.getAverageKneeAngle();
        double backAngle = frameData.getBackAngle();

        boolean validStandingPosition = kneeAngle >= 160.0;
        boolean validBottomPosition = kneeAngle <= 100.0;
        boolean validForm = backAngle >= 140.0;

        if (!repInProgress) {
            if (validStandingPosition && validForm) {
                repInProgress = true;
                reachedBottom = false;
                formWasValidThroughoutRep = true;
                return RepTrackingResult.ready("Squat ready");
            }

            return RepTrackingResult.invalid(
                    false,
                    "Stand upright to begin squat"
            );
        }

        if (!validForm) {
            formWasValidThroughoutRep = false;
        }

        if (!reachedBottom) {
            if (validBottomPosition) {
                reachedBottom = true;
                return RepTrackingResult.bottomReached(
                        formWasValidThroughoutRep,
                        "Squat depth reached"
                );
            }

            return RepTrackingResult.descending(
                    formWasValidThroughoutRep,
                    "Lower into the squat"
            );
        }

        if (validStandingPosition) {
            boolean validRep = formWasValidThroughoutRep && validForm;
            reset();

            if (validRep) {
                return RepTrackingResult.completed("Squat rep counted");
            }

            return RepTrackingResult.invalid(
                    true,
                    "Rep not counted. Keep your chest up"
            );
        }

        return RepTrackingResult.ascending(
                formWasValidThroughoutRep,
                "Drive back up"
        );
    }

    @NonNull
    private RepTrackingResult analyzeSitUpRep(@NonNull PoseFrameData frameData) {
        double hipAngle = frameData.getAverageHipAngle();

        boolean validStartPosition = hipAngle >= 140.0;
        boolean validTopPosition = hipAngle <= 90.0;

        if (!repInProgress) {
            if (validStartPosition) {
                repInProgress = true;
                reachedBottom = false;
                formWasValidThroughoutRep = true;
                return RepTrackingResult.ready("Sit-up ready");
            }

            return RepTrackingResult.invalid(
                    false,
                    "Lower fully to begin sit-up"
            );
        }

        if (!reachedBottom) {
            if (validTopPosition) {
                reachedBottom = true;
                return RepTrackingResult.bottomReached(
                        true,
                        "Sit-up top reached"
                );
            }

            return RepTrackingResult.descending(
                    true,
                    "Lift your upper body"
            );
        }

        if (validStartPosition) {
            reset();
            return RepTrackingResult.completed("Sit-up rep counted");
        }

        return RepTrackingResult.ascending(
                true,
                "Lower with control"
        );
    }

    public void reset() {
        repInProgress = false;
        reachedBottom = false;
        formWasValidThroughoutRep = true;
    }
}