package com.example.smartfit.workout.engine;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.smartfit.workout.model.PoseFrameData;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

public class PoseFrameAnalyzer implements ImageAnalysis.Analyzer {

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

    private final PoseDetector poseDetector;
    private final PoseDataMapper poseDataMapper;
    private final Listener listener;

    private boolean isProcessing = false;

    public PoseFrameAnalyzer(@NonNull Listener listener) {
        this.listener = listener;
        this.poseDataMapper = new PoseDataMapper();

        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();

        poseDetector = PoseDetection.getClient(options);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        if (isProcessing) {
            imageProxy.close();
            return;
        }

        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        isProcessing = true;

        int width = imageProxy.getWidth();
        int height = imageProxy.getHeight();
        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                rotationDegrees
        );

        poseDetector.process(image)
                .addOnSuccessListener(pose -> {
                    if (pose.getAllPoseLandmarks().isEmpty()) {
                        listener.onPoseMissing();
                    } else {
                        PoseFrameData frameData = poseDataMapper.map(pose);
                        listener.onPoseDataReady(
                                frameData,
                                pose,
                                width,
                                height,
                                rotationDegrees
                        );
                    }
                })
                .addOnFailureListener(listener::onError)
                .addOnCompleteListener(task -> {
                    isProcessing = false;
                    imageProxy.close();
                });
    }

    public void stop() {
        poseDetector.close();
    }

    public void reset(){
        poseDataMapper.reset();
    }
}