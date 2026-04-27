package com.example.smartfit.workout.haptic;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.smartfit.workout.model.WorkoutFeedback;
import com.example.smartfit.workout.model.enums.FeedbackLevel;

/**
 * Provides haptic feedback during a workout session.
 *
 * Mirrors the structure of WorkoutVoiceCoach:
 *  - can be enabled/disabled
 *  - maps FeedbackLevel to distinct vibration patterns
 *  - respects a minimum interval to avoid buzzing on every frame
 *
 * Vibration patterns:
 *  GOOD    — single short pulse   (50ms)   subtle positive confirmation
 *  INFO    — double tap           (50, 50, 50ms)  gentle nudge
 *  WARNING — strong single pulse  (200ms)  clear form-break alert
 *  ERROR   — waveform             (100, 100, 200ms)  urgent / terminal
 */
public class WorkoutHapticCoach {

    // =========================================================
    // TIMING
    // =========================================================
    private static final long MIN_HAPTIC_INTERVAL_MILLIS = 800L;

    // =========================================================
    // VIBRATION PATTERNS (pause, vibrate, pause, vibrate …)
    // =========================================================
    private static final long[] PATTERN_GOOD    = {0, 50};
    private static final long[] PATTERN_INFO    = {0, 50, 60, 50};
    private static final long[] PATTERN_WARNING = {0, 200};
    private static final long[] PATTERN_ERROR   = {0, 100, 100, 200};

    // =========================================================
    // AMPLITUDE (API 26+) — 0–255, -1 = device default
    // =========================================================
    private static final int AMP_GOOD    = 80;
    private static final int AMP_INFO    = 100;
    private static final int AMP_WARNING = 200;
    private static final int AMP_ERROR   = 255;

    // =========================================================
    // CORE
    // =========================================================
    @Nullable
    private final Vibrator vibrator;

    private boolean enabled = true;

    private long lastHapticAtMillis = 0L;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public WorkoutHapticCoach(@NonNull Context context) {
        Context appContext = context.getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) appContext
                    .getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = (vm != null) ? vm.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    // =========================================================
    // SETTINGS
    // =========================================================
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled && vibrator != null && vibrator.hasVibrator();
    }

    // =========================================================
    // PUBLIC API
    // =========================================================

    /**
     * Vibrate according to the feedback level. Respects the minimum interval
     * so rapid-fire feedback frames don't create a continuous buzz.
     */
    public void vibrateForFeedback(@Nullable WorkoutFeedback feedback) {
        if (feedback == null || !isEnabled()) {
            return;
        }

        // GOOD feedback is subtle — skip it unless the caller really wants it
        FeedbackLevel level = feedback.getLevel();
        if (level == FeedbackLevel.GOOD) {
            return;
        }

        if (!canVibrateNow()) {
            return;
        }

        vibrateForLevel(level);
    }

    /**
     * Vibrate for a specific level directly — used for rep-counted confirmation,
     * session start/stop signals, etc.
     */
    public void vibrateForLevel(@NonNull FeedbackLevel level) {
        if (!isEnabled()) {
            return;
        }

        lastHapticAtMillis = System.currentTimeMillis();

        switch (level) {
            case GOOD:
                vibrate(PATTERN_GOOD, AMP_GOOD);
                break;
            case INFO:
                vibrate(PATTERN_INFO, AMP_INFO);
                break;
            case WARNING:
                vibrate(PATTERN_WARNING, AMP_WARNING);
                break;
            case ERROR:
                vibrate(PATTERN_ERROR, AMP_ERROR);
                break;
        }
    }

    /**
     * Single crisp pulse — use for rep count confirmation.
     */
    public void vibrateRepConfirm() {
        if (!isEnabled()) {
            return;
        }
        lastHapticAtMillis = System.currentTimeMillis();
        vibrate(PATTERN_GOOD, AMP_GOOD);
    }

    /**
     * Double pulse — use for session start/resume.
     */
    public void vibrateSessionStart() {
        if (!isEnabled()) {
            return;
        }
        lastHapticAtMillis = System.currentTimeMillis();
        vibrate(PATTERN_INFO, AMP_INFO);
    }

    /**
     * Strong single buzz — use for session end or goal reached.
     */
    public void vibrateSessionEnd() {
        if (!isEnabled()) {
            return;
        }
        lastHapticAtMillis = System.currentTimeMillis();
        vibrate(PATTERN_WARNING, AMP_WARNING);
    }

    public void cancel() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    // =========================================================
    // INTERNALS
    // =========================================================
    private boolean canVibrateNow() {
        return (System.currentTimeMillis() - lastHapticAtMillis) >= MIN_HAPTIC_INTERVAL_MILLIS;
    }

    private void vibrate(long[] pattern, int amplitude) {
        if (vibrator == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int[] amplitudes = buildAmplitudes(pattern, amplitude);
            assert vibrator != null;
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1));
        } else {
            vibrator.vibrate(pattern, -1);
        }
    }

    /**
     * Build an amplitudes array that matches the pattern length.
     * Pause slots (even indices) get amplitude 0; vibrate slots get the given amplitude.
     */
    private int[] buildAmplitudes(long[] pattern, int amplitude) {
        int[] amplitudes = new int[pattern.length];
        for (int i = 0; i < pattern.length; i++) {
            amplitudes[i] = (i % 2 == 0) ? 0 : amplitude;
        }
        return amplitudes;
    }
}
