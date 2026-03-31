package com.example.smartfit.workout.engine;

import android.graphics.Matrix;
import android.graphics.PointF;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PoseOverlayMapper {

    @NonNull
    public List<PointF> mapToOverlay(
            @NonNull Pose pose,
            int imageWidth,
            int imageHeight,
            int rotationDegrees,
            @NonNull PreviewView previewView,
            boolean isFrontCamera
    ) {
        List<PointF> mappedPoints = new ArrayList<>(Collections.nCopies(33, null));

        float viewWidth = previewView.getWidth();
        float viewHeight = previewView.getHeight();

        if (viewWidth <= 0 || viewHeight <= 0 || imageWidth <= 0 || imageHeight <= 0) {
            return mappedPoints;
        }

        Matrix transform = buildImageToViewTransform(
                imageWidth,
                imageHeight,
                rotationDegrees,
                viewWidth,
                viewHeight,
                isFrontCamera
        );

        float[] point = new float[2];

        for (PoseLandmark landmark : pose.getAllPoseLandmarks()) {
            int type = landmark.getLandmarkType();

            point[0] = landmark.getPosition().x;
            point[1] = landmark.getPosition().y;

            transform.mapPoints(point);

            if (type >= 0 && type < mappedPoints.size()) {
                mappedPoints.set(type, new PointF(point[0], point[1]));
            }
        }

        return mappedPoints;
    }

    @NonNull
    private Matrix buildImageToViewTransform(
            int imageWidth,
            int imageHeight,
            int rotationDegrees,
            float viewWidth,
            float viewHeight,
            boolean isFrontCamera
    ) {
        Matrix matrix = new Matrix();

        float imageCenterX = imageWidth / 2f;
        float imageCenterY = imageHeight / 2f;

        // -----------------------------------------------------
        // Step 1: Rotate image-space coordinates into upright view-space
        // -----------------------------------------------------
        matrix.postRotate(rotationDegrees, imageCenterX, imageCenterY);

        // After rotation, the effective image width/height may swap
        boolean swapDimensions = rotationDegrees == 90 || rotationDegrees == 270;
        float rotatedWidth = swapDimensions ? imageHeight : imageWidth;
        float rotatedHeight = swapDimensions ? imageWidth : imageHeight;

        // -----------------------------------------------------
        // Step 2: Move rotated coordinates so top-left starts at (0,0)
        // -----------------------------------------------------
        RectBounds rotatedBounds = mapImageBoundsAfterRotation(
                imageWidth,
                imageHeight,
                rotationDegrees
        );
        matrix.postTranslate(-rotatedBounds.minX, -rotatedBounds.minY);

        // -----------------------------------------------------
        // Step 3: Scale using center-crop logic to match PreviewView fillCenter
        // -----------------------------------------------------
        float scale = Math.max(viewWidth / rotatedWidth, viewHeight / rotatedHeight);
        matrix.postScale(scale, scale);

        float scaledWidth = rotatedWidth * scale;
        float scaledHeight = rotatedHeight * scale;

        float dx = (viewWidth - scaledWidth) / 2f;
        float dy = (viewHeight - scaledHeight) / 2f;

        matrix.postTranslate(dx, dy);

        // -----------------------------------------------------
        // Step 4: Mirror horizontally for front camera
        // -----------------------------------------------------
        if (isFrontCamera) {
            matrix.postScale(-1f, 1f, viewWidth / 2f, viewHeight / 2f);
        }

        return matrix;
    }

    @NonNull
    private RectBounds mapImageBoundsAfterRotation(
            int imageWidth,
            int imageHeight,
            int rotationDegrees
    ) {
        Matrix rotationMatrix = new Matrix();
        float centerX = imageWidth / 2f;
        float centerY = imageHeight / 2f;
        rotationMatrix.postRotate(rotationDegrees, centerX, centerY);

        float[] corners = new float[]{
                0f, 0f,
                imageWidth, 0f,
                imageWidth, imageHeight,
                0f, imageHeight
        };

        rotationMatrix.mapPoints(corners);

        float minX = corners[0];
        float maxX = corners[0];
        float minY = corners[1];
        float maxY = corners[1];

        for (int i = 2; i < corners.length; i += 2) {
            float x = corners[i];
            float y = corners[i + 1];

            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        return new RectBounds(minX, minY, maxX, maxY);
    }

    public List<int[]> getConnections() {
        List<int[]> connections = new ArrayList<>();

        connections.add(new int[]{PoseLandmark.LEFT_EYE_INNER, PoseLandmark.LEFT_EYE});
        connections.add(new int[]{PoseLandmark.LEFT_EYE, PoseLandmark.LEFT_EYE_OUTER});
        connections.add(new int[]{PoseLandmark.RIGHT_EYE_INNER, PoseLandmark.RIGHT_EYE});
        connections.add(new int[]{PoseLandmark.RIGHT_EYE, PoseLandmark.RIGHT_EYE_OUTER});
        connections.add(new int[]{PoseLandmark.LEFT_MOUTH, PoseLandmark.RIGHT_MOUTH});

        connections.add(new int[]{PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER});
        connections.add(new int[]{PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW});
        connections.add(new int[]{PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST});
        connections.add(new int[]{PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW});
        connections.add(new int[]{PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST});

        connections.add(new int[]{PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP});
        connections.add(new int[]{PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP});
        connections.add(new int[]{PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP});

        connections.add(new int[]{PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE});
        connections.add(new int[]{PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE});
        connections.add(new int[]{PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE});
        connections.add(new int[]{PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE});

        connections.add(new int[]{PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_HEEL});
        connections.add(new int[]{PoseLandmark.LEFT_HEEL, PoseLandmark.LEFT_FOOT_INDEX});
        connections.add(new int[]{PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_HEEL});
        connections.add(new int[]{PoseLandmark.RIGHT_HEEL, PoseLandmark.RIGHT_FOOT_INDEX});

        return connections;
    }

    private static class RectBounds {
        final float minX;
        final float minY;
        final float maxX;
        final float maxY;

        RectBounds(float minX, float minY, float maxX, float maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }
}