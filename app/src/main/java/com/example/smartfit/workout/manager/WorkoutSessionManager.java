package com.example.smartfit.workout.manager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.smartfit.workout.model.PoseFrameData;
import com.example.smartfit.workout.model.RepTrackingResult;
import com.example.smartfit.workout.model.WorkoutFeedback;
import com.example.smartfit.workout.model.WorkoutPreferences;
import com.example.smartfit.workout.model.WorkoutSession;
import com.example.smartfit.workout.model.enums.FeedbackLevel;
import com.example.smartfit.workout.model.enums.FormBreakAction;
import com.example.smartfit.workout.model.enums.WorkoutState;
import com.example.smartfit.workout.model.enums.WorkoutType;
import com.example.smartfit.workout.repository.WorkoutRepository;
import com.example.smartfit.workout.engine.PoseAnalyzer;
import com.example.smartfit.workout.engine.WorkoutFeedbackEngine;
import com.example.smartfit.workout.haptic.WorkoutHapticCoach;

public class WorkoutSessionManager {

    public interface Listener {
        void onRepUpdated(int repCount);
        void onDurationUpdated(long remainingDurationMillis, long holdDurationMillis);
        void onFeedbackUpdated(@NonNull WorkoutFeedback feedback);
        void onSessionPaused(@NonNull WorkoutSession session);
        void onSessionResumed(@NonNull WorkoutSession session);
        void onSessionCompleted(@NonNull WorkoutSession session);
        void onSessionCancelled(@NonNull WorkoutSession session);
    }

    private final WorkoutRepository repository;
    private final WorkoutFeedbackEngine feedbackEngine;
    private final PoseAnalyzer poseAnalyzer;
    @NonNull
    private final WorkoutHapticCoach hapticCoach;
    @Nullable
    private final Listener listener;

    private WorkoutSession currentSession;
    private WorkoutPreferences preferences;

    private long lastPlankProgressTimestampMillis = 0L;

    public WorkoutSessionManager(
            @NonNull WorkoutRepository repository,
            @NonNull WorkoutFeedbackEngine feedbackEngine,
            @NonNull PoseAnalyzer poseAnalyzer,
            @NonNull WorkoutHapticCoach hapticCoach,
            @Nullable Listener listener
    ) {
        this.repository = repository;
        this.feedbackEngine = feedbackEngine;
        this.poseAnalyzer = poseAnalyzer;
        this.hapticCoach = hapticCoach;
        this.listener = listener;
        this.preferences = new WorkoutPreferences();
    }

    public void setPreferences(@Nullable WorkoutPreferences preferences) {
        if (preferences != null) {
            this.preferences = preferences;
        }
    }

    @NonNull
    public WorkoutPreferences getPreferences() {
        return preferences;
    }

    @Nullable
    public WorkoutSession getCurrentSession() {
        return currentSession;
    }

    public void startSession(@NonNull WorkoutType workoutType) {
        long now = System.currentTimeMillis();
        long targetDurationMillis = workoutType.isDurationBased()
                ? resolveTargetDurationMillis(workoutType)
                : 0L;

        currentSession = new WorkoutSession(
                now,
                workoutType,
                now,
                targetDurationMillis
        );

        poseAnalyzer.reset();
        lastPlankProgressTimestampMillis = 0L;

        dispatchFeedback(WorkoutFeedback.info(workoutType.getDisplayName() + " session started"));
        hapticCoach.vibrateSessionStart();

        if (listener != null) {
            listener.onRepUpdated(currentSession.getRepCount());

            if (currentSession.isDurationBased()) {
                listener.onDurationUpdated(
                        currentSession.getRemainingDurationMillis(),
                        currentSession.getHoldDurationMillis()
                );
            }
        }

    }

    public void pauseSession() {
        if (currentSession == null || currentSession.isFinished() || currentSession.isPaused()) {
            return;
        }

        currentSession.pause(System.currentTimeMillis());
        lastPlankProgressTimestampMillis = 0L;

        dispatchFeedback(WorkoutFeedback.info("Session paused"));

        if (listener != null) {
            listener.onSessionPaused(currentSession);
        }
    }

    public void resumeSession() {
        if (currentSession == null || currentSession.isFinished() || !currentSession.isPaused()) {
            return;
        }

        currentSession.resume(System.currentTimeMillis());
        lastPlankProgressTimestampMillis = 0L;

        dispatchFeedback(WorkoutFeedback.info("Session resumed"));

        if (listener != null) {
            listener.onSessionResumed(currentSession);
        }
    }

    public void endSession() {
        if (currentSession == null || currentSession.isFinished()) {
            return;
        }

        currentSession.complete(System.currentTimeMillis());
        repository.saveSession(currentSession);

        if (listener != null) {
            listener.onSessionCompleted(currentSession);
        }

        hapticCoach.vibrateSessionEnd();

        currentSession = null;
        lastPlankProgressTimestampMillis = 0L;
        poseAnalyzer.reset();
    }

    public void cancelSession() {
        if (currentSession == null || currentSession.isFinished()) {
            return;
        }

        currentSession.cancel(System.currentTimeMillis());
        repository.saveSession(currentSession);

        if (listener != null) {
            listener.onSessionCancelled(currentSession);
        }

        hapticCoach.vibrateSessionEnd();

        currentSession = null;
        lastPlankProgressTimestampMillis = 0L;
        poseAnalyzer.reset();
    }

    public void processFrame(@NonNull PoseFrameData frameData) {
        if (currentSession == null || currentSession.getState() != WorkoutState.STARTED) {
            return;
        }

        WorkoutType workoutType = currentSession.getWorkoutType();

        if (workoutType.isRepBased()) {
            processRepWorkout(frameData);
        } else if (workoutType.isDurationBased()) {
            processDurationWorkout(frameData);
        }
    }

    public void onPoseMissing() {
        if (currentSession == null || currentSession.getState() != WorkoutState.STARTED) {
            return;
        }

        if (currentSession.isDurationBased()
                && preferences.isPlankCountdownRequiresCorrectPose()) {

            lastPlankProgressTimestampMillis = 0L;

            if (preferences.getPlankFormBreakAction() == FormBreakAction.CANCEL_SESSION) {
                dispatchFeedback(WorkoutFeedback.error("Pose lost. Session cancelled."));
                cancelSession();
            } else {
                dispatchFeedback(WorkoutFeedback.warning("Pose lost. Countdown paused."));
            }

            return;
        }

        dispatchFeedback(WorkoutFeedback.warning("No full body detected."));
    }

    private void processRepWorkout(@NonNull PoseFrameData frameData) {
        RepTrackingResult repResult = poseAnalyzer.analyzeRep(
                currentSession.getWorkoutType(),
                frameData
        );

        WorkoutFeedback feedback = feedbackEngine.analyze(
                currentSession.getWorkoutType(),
                frameData
        );

        dispatchFeedback(feedback);

        if (repResult.isRepCompleted()) {
            currentSession.incrementRepCount();

            hapticCoach.vibrateRepConfirm();
            if (listener != null) {
                listener.onRepUpdated(currentSession.getRepCount());
            }

            dispatchFeedback(WorkoutFeedback.info(
                    currentSession.getWorkoutType().getDisplayName() + " rep counted"
            ));
        } else if (repResult.isInvalid() && repResult.shouldResetTracking()) {
            poseAnalyzer.reset();
        }
    }

    private void processDurationWorkout(@NonNull PoseFrameData frameData) {
        WorkoutFeedback feedback = feedbackEngine.analyze(
                currentSession.getWorkoutType(),
                frameData
        );

        dispatchFeedback(feedback);

        boolean validPose = feedback.isCorrectForm();
        long now = System.currentTimeMillis();

        if (preferences.isPlankCountdownRequiresCorrectPose() && !validPose) {
            lastPlankProgressTimestampMillis = 0L;

            if (preferences.getPlankFormBreakAction() == FormBreakAction.CANCEL_SESSION) {
                dispatchFeedback(WorkoutFeedback.error("Form broken. Session cancelled."));
                cancelSession();
            } else {
                dispatchFeedback(WorkoutFeedback.warning("Form broken. Countdown paused."));
            }
            return;
        }

        if (lastPlankProgressTimestampMillis == 0L) {
            lastPlankProgressTimestampMillis = now;
            return;
        }

        long deltaMillis = Math.max(0L, now - lastPlankProgressTimestampMillis);
        lastPlankProgressTimestampMillis = now;

        currentSession.setHoldDurationMillis(
                currentSession.getHoldDurationMillis() + deltaMillis
        );
        currentSession.reduceRemainingDurationMillis(deltaMillis);

        if (listener != null) {
            listener.onDurationUpdated(
                    currentSession.getRemainingDurationMillis(),
                    currentSession.getHoldDurationMillis()
            );
        }

        if (currentSession.isDurationGoalReached()) {
            dispatchFeedback(WorkoutFeedback.info("Plank complete"));
            endSession();
        }
    }

    private long resolveTargetDurationMillis(@NonNull WorkoutType workoutType) {
        if (workoutType == WorkoutType.PLANK) {
            return preferences.getPlankTargetDurationMillis();
        }
        return workoutType.getDefaultTargetDurationMillis();
    }

    private void dispatchFeedback(@NonNull WorkoutFeedback feedback) {
        if (currentSession != null) {
            currentSession.setLastFeedback(feedback.getMessage());
        }

        // Haptic mirrors voice — WARNING/ERROR get a buzz, GOOD/INFO are silent
        // by default inside vibrateForFeedback(). This runs on the calling thread
        // (camera background thread) which is fine — Vibrator is thread-safe.
        hapticCoach.vibrateForFeedback(feedback);

        if (listener != null) {
            listener.onFeedbackUpdated(feedback);
        }
    }
}