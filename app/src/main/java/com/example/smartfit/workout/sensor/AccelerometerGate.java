package com.example.smartfit.workout.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * AccelerometerGate
 *
 * Listens to TYPE_ACCELEROMETER and decides whether significant motion is
 * happening. When the phone is lying still on a bench (or the user has
 * stopped moving), the gate closes and PoseFrameAnalyzer should skip
 * inference — saving CPU/GPU and battery.
 *
 * Usage:
 *   gate.start();
 *   // in PoseFrameAnalyzer.analyze():
 *   if (!gate.isMotionDetected()) { imageProxy.close(); return; }
 *   gate.stop(); // when session ends
 *
 * Algorithm:
 *   We track a short exponential moving average of the total acceleration
 *   magnitude (minus gravity = 9.8 m/s²). If the variance from gravity is
 *   below a configurable threshold for a configurable hold window, the gate
 *   closes. The gate re-opens as soon as movement resumes.
 *
 *   This is intentionally simple — the goal is to catch the "phone left on
 *   a bench" case, not to classify exercise types.
 */
public class AccelerometerGate implements SensorEventListener {

    // =========================================================
    // DEFAULTS
    // =========================================================
    /** m/s² deviation from gravity below which a sample is "still". */
    private static final float DEFAULT_STILL_THRESHOLD = 0.8f;

    /**
     * How many consecutive still samples before the gate closes.
     * At SENSOR_DELAY_GAME (~50 Hz) this is about 1.2 seconds.
     */
    private static final int DEFAULT_STILL_COUNT_REQUIRED = 60;

    /** Sensor sampling rate — GAME gives ~50 Hz, sufficient for motion detection. */
    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;

    // =========================================================
    // STATE
    // =========================================================
    @Nullable
    private final SensorManager sensorManager;

    @Nullable
    private final Sensor accelerometer;

    @Nullable
    private Listener listener;

    private final float stillThreshold;
    private final int stillCountRequired;

    private boolean running = false;
    private boolean motionDetected = true;   // open by default so we don't block at startup
    private int stillSampleCount = 0;

    // =========================================================
    // CALLBACK INTERFACE
    // =========================================================
    public interface Listener {
        /** Called once when the device transitions from moving → still. */
        void onMotionStopped();
        /** Called once when the device transitions from still → moving. */
        void onMotionResumed();
    }

    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    public AccelerometerGate(@NonNull Context context) {
        this(context, DEFAULT_STILL_THRESHOLD, DEFAULT_STILL_COUNT_REQUIRED, null);
    }

    public AccelerometerGate(
            @NonNull Context context,
            float stillThreshold,
            int stillCountRequired,
            @Nullable Listener listener
    ) {
        Context appContext = context.getApplicationContext();
        sensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = (sensorManager != null)
                ? sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                : null;

        this.stillThreshold = stillThreshold;
        this.stillCountRequired = Math.max(1, stillCountRequired);
        this.listener = listener;
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================
    public void start() {
        if (running || sensorManager == null || accelerometer == null) {
            return;
        }

        motionDetected = true;
        stillSampleCount = 0;
        running = true;

        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY);
    }

    public void stop() {
        if (!running || sensorManager == null) {
            return;
        }

        sensorManager.unregisterListener(this);
        running = false;
        motionDetected = true;   // reset to open so next start() isn't blocked
        stillSampleCount = 0;
    }

    // =========================================================
    // PUBLIC QUERY
    // =========================================================
    /**
     * Returns true if significant motion has been detected recently.
     * PoseFrameAnalyzer should call this before running inference.
     */
    public boolean isMotionDetected() {
        return motionDetected;
    }

    /** True if the accelerometer sensor is available on this device. */
    public boolean isAvailable() {
        return accelerometer != null;
    }

    // =========================================================
    // SENSOR EVENTS
    // =========================================================
    @Override
    public void onSensorChanged(@NonNull SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Total vector magnitude
        double magnitude = Math.sqrt((double) x * x + (double) y * y + (double) z * z);

        // Deviation from resting gravity (9.81 m/s²)
        double deviation = Math.abs(magnitude - SensorManager.GRAVITY_EARTH);

        if (deviation < stillThreshold) {
            stillSampleCount++;

            if (stillSampleCount >= stillCountRequired && motionDetected) {
                motionDetected = false;
                if (listener != null) {
                    listener.onMotionStopped();
                }
            }
        } else {
            stillSampleCount = 0;

            if (!motionDetected) {
                motionDetected = true;
                if (listener != null) {
                    listener.onMotionResumed();
                }
            }
        }
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onAccuracyChanged(@NonNull Sensor sensor, int accuracy) {
        // Not needed
    }
}
