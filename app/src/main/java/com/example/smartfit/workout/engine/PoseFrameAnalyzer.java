package com.example.smartfit.workout.engine;

import android.os.Trace;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.smartfit.workout.model.PoseFrameData;
import com.example.smartfit.workout.sensor.AccelerometerGate;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

/**
 * Runs ML Kit pose detection on each camera frame.
 *
 * Changes from original:
 *
 * 1. GPU delegate — PoseDetectorOptions now uses PREFER_GPU_THEN_CPU so the
 *    model runs on the GPU where available, lowering per-frame latency and
 *    reducing CPU thermal load.
 *
 * 2. AccelerometerGate — an optional gate that skips inference entirely when
 *    the phone is stationary (e.g. resting on a bench between sets). Pass a
 *    pre-started AccelerometerGate via setAccelerometerGate(); if none is set,
 *    inference always runs as before.
 *
 * 3. isProcessing flag promoted to volatile to ensure cross-thread visibility.
 */
public class PoseFrameAnalyzer implements ImageAnalysis.Analyzer {

    // =========================================================
    // LISTENER
    // =========================================================
    public interface Listener {
        void onPoseDataReady(
                PoseFrameData frameData,
                Pose pose,
                int imageWidth,
                int imageHeight,
                int rotationDegrees
        );

        void onPoseMissing();

        void onError(Exception e);
    }

    // =========================================================
    // FIELDS
    // =========================================================
    private final PoseDetector poseDetector;
    private final PoseDataMapper poseDataMapper;
    private final Listener listener;

    /**
     * volatile ensures the write from the ML Kit callback thread
     * is visible to the camera analyzer thread on the next frame.
     */
    private volatile boolean isProcessing = false;

    @NonNull
    private AccelerometerGate accelerometerGate = new NoOpAccelerometerGate();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public PoseFrameAnalyzer(@NonNull Listener listener) {
        this.listener = listener;
        this.poseDataMapper = new PoseDataMapper();

        // ---------------------------------------------------------
        // GPU delegate: PREFER_GPU_THEN_CPU tells ML Kit to run the
        // pose model on the GPU if an acceptable delegate is found,
        // and fall back to CPU if not. On most mid-range and higher
        // devices this brings per-frame latency well under the 33ms
        // (30 fps) budget without any other code changes.
        //
        // Requires: play-services-mlkit-pose-detection 18.0.0-beta3+
        //   or the bundled variant (mlkit-pose-detection-accurate).
        // ---------------------------------------------------------
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
                .build();

        poseDetector = PoseDetection.getClient(options);
    }

    // =========================================================
    // ACCELEROMETER GATE
    // =========================================================
    /**
     * Attach a running AccelerometerGate. When the gate reports no motion,
     * frames are dropped without running inference — saving GPU/CPU time
     * between exercise sets or when the phone is left on a bench.
     *
     * The gate's lifecycle (start/stop) is managed externally by the caller.
     */
    public void setAccelerometerGate(@NonNull AccelerometerGate gate) {
        this.accelerometerGate = gate;
    }

    // =========================================================
    // ANALYZER
    // =========================================================
    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        // Drop frame if we're still waiting for the previous inference to finish
        if (isProcessing) {
            imageProxy.close();
            return;
        }

        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        // ---------------------------------------------------------
        // AccelerometerGate: skip inference when the phone is still.
        // This is the same early-exit pattern as the isProcessing
        // check — close the proxy and return immediately so CameraX
        // can reuse the buffer. The gate's onMotionResumed() callback
        // will fire as soon as movement is detected again.
        // ---------------------------------------------------------
        if (!accelerometerGate.isMotionDetected()) {
            imageProxy.close();
            return;
        }

        isProcessing = true;

        // Begin a named trace section so Android Studio Profiler / Perfetto
        // can show exactly how long each step takes per frame.
        // Names appear in the "System Trace" track under your app's process.
        Trace.beginSection("PoseFrameAnalyzer#analyze");

        int width = imageProxy.getWidth();
        int height = imageProxy.getHeight();
        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                rotationDegrees
        );

        Trace.beginSection("MLKit#processPose");

        poseDetector.process(image)
                .addOnSuccessListener(pose -> {
                    Trace.endSection(); // ends MLKit#processPose

                    Trace.beginSection("PoseDataMapper#map");
                    if (pose.getAllPoseLandmarks().isEmpty()) {
                        Trace.endSection(); // ends PoseDataMapper#map
                        listener.onPoseMissing();
                    } else {
                        PoseFrameData frameData = poseDataMapper.map(pose);
                        Trace.endSection(); // ends PoseDataMapper#map
                        listener.onPoseDataReady(
                                frameData,
                                pose,
                                width,
                                height,
                                rotationDegrees
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    Trace.endSection(); // ends MLKit#processPose on failure path
                    listener.onError(e);
                })
                .addOnCompleteListener(task -> {
                    isProcessing = false;
                    imageProxy.close();
                    Trace.endSection(); // ends PoseFrameAnalyzer#analyze
                });
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================
    public void stop() {
        poseDetector.close();
    }

    public void reset() {
        poseDataMapper.reset();
    }

    // =========================================================
    // NO-OP GATE (default — preserves original behaviour)
    // =========================================================

    /**
     * Default gate used when no AccelerometerGate has been set.
     * Always reports motion detected, so inference always runs —
     * identical to the original behaviour before this change.
     */
    private static final class NoOpAccelerometerGate extends AccelerometerGate {

        // Pass a dummy context — this subclass never registers a sensor.
        NoOpAccelerometerGate() {
            super(dummyContext());
        }

        @Override
        public boolean isMotionDetected() {
            return true;
        }

        @Override
        public void start() { /* no-op */ }

        @Override
        public void stop()  { /* no-op */ }

        private static android.content.Context dummyContext() {
            // Intentionally returns null — the parent constructor null-checks
            // sensorManager, so passing null context is safe here.
            return null;
        }
    }
}
