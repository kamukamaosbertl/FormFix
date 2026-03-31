package com.example.smartfit.workout.engine;

import androidx.annotation.NonNull;

import com.example.smartfit.workout.model.PoseFrameData;
import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

public class PoseDataMapper {

    // =========================================================
    // SMOOTHERS
    // Small moving-average window to reduce noisy readings
    // =========================================================
    private final AngleSmoother leftElbowSmoother = new AngleSmoother(5);
    private final AngleSmoother rightElbowSmoother = new AngleSmoother(5);
    private final AngleSmoother leftKneeSmoother = new AngleSmoother(5);
    private final AngleSmoother rightKneeSmoother = new AngleSmoother(5);
    private final AngleSmoother leftHipSmoother = new AngleSmoother(5);
    private final AngleSmoother rightHipSmoother = new AngleSmoother(5);
    private final AngleSmoother backAngleSmoother = new AngleSmoother(5);

    @NonNull
    public PoseFrameData map(@NonNull Pose pose) {
        double leftElbowAngle = leftElbowSmoother.smooth(
                get3DAngle(
                        pose,
                        PoseLandmark.LEFT_SHOULDER,
                        PoseLandmark.LEFT_ELBOW,
                        PoseLandmark.LEFT_WRIST
                )
        );

        double rightElbowAngle = rightElbowSmoother.smooth(
                get3DAngle(
                        pose,
                        PoseLandmark.RIGHT_SHOULDER,
                        PoseLandmark.RIGHT_ELBOW,
                        PoseLandmark.RIGHT_WRIST
                )
        );

        double leftKneeAngle = leftKneeSmoother.smooth(
                get3DAngle(
                        pose,
                        PoseLandmark.LEFT_HIP,
                        PoseLandmark.LEFT_KNEE,
                        PoseLandmark.LEFT_ANKLE
                )
        );

        double rightKneeAngle = rightKneeSmoother.smooth(
                get3DAngle(
                        pose,
                        PoseLandmark.RIGHT_HIP,
                        PoseLandmark.RIGHT_KNEE,
                        PoseLandmark.RIGHT_ANKLE
                )
        );

        double leftHipAngle = leftHipSmoother.smooth(
                get3DAngle(
                        pose,
                        PoseLandmark.LEFT_SHOULDER,
                        PoseLandmark.LEFT_HIP,
                        PoseLandmark.LEFT_KNEE
                )
        );

        double rightHipAngle = rightHipSmoother.smooth(
                get3DAngle(
                        pose,
                        PoseLandmark.RIGHT_SHOULDER,
                        PoseLandmark.RIGHT_HIP,
                        PoseLandmark.RIGHT_KNEE
                )
        );

        double backAngle = backAngleSmoother.smooth(getBackAngle3D(pose));

        return new PoseFrameData(
                leftElbowAngle,
                rightElbowAngle,
                leftKneeAngle,
                rightKneeAngle,
                leftHipAngle,
                rightHipAngle,
                backAngle
        );
    }

    public void reset() {
        leftElbowSmoother.reset();
        rightElbowSmoother.reset();
        leftKneeSmoother.reset();
        rightKneeSmoother.reset();
        leftHipSmoother.reset();
        rightHipSmoother.reset();
        backAngleSmoother.reset();
    }

    // =========================================================
    // 3D JOINT ANGLE
    // Angle at B from three landmarks A-B-C using:
    // BA = A - B
    // BC = C - B
    // angle = acos(dot(BA, BC) / (|BA| * |BC|))
    // =========================================================
    private double get3DAngle(
            @NonNull Pose pose,
            int firstPoint,
            int midPoint,
            int lastPoint
    ) {
        PoseLandmark p1 = pose.getPoseLandmark(firstPoint);
        PoseLandmark p2 = pose.getPoseLandmark(midPoint);
        PoseLandmark p3 = pose.getPoseLandmark(lastPoint);

        if (p1 == null || p2 == null || p3 == null) {
            return 180.0;
        }

        PointF3D a = p1.getPosition3D();
        PointF3D b = p2.getPosition3D();
        PointF3D c = p3.getPosition3D();

        float bax = a.getX() - b.getX();
        float bay = a.getY() - b.getY();
        float baz = a.getZ() - b.getZ();

        float bcx = c.getX() - b.getX();
        float bcy = c.getY() - b.getY();
        float bcz = c.getZ() - b.getZ();

        double dot = (bax * bcx) + (bay * bcy) + (baz * bcz);
        double magBA = Math.sqrt((bax * bax) + (bay * bay) + (baz * baz));
        double magBC = Math.sqrt((bcx * bcx) + (bcy * bcy) + (bcz * bcz));

        if (magBA == 0.0 || magBC == 0.0) {
            return 180.0;
        }

        double cosTheta = dot / (magBA * magBC);

        // Clamp to avoid NaN from floating-point rounding
        cosTheta = Math.max(-1.0, Math.min(1.0, cosTheta));

        return Math.toDegrees(Math.acos(cosTheta));
    }

    // =========================================================
    // BACK ANGLE
    // Use shoulder-hip-ankle chain in 3D on both sides, then average
    // =========================================================
    private double getBackAngle3D(@NonNull Pose pose) {
        double leftBackAngle = get3DAngle(
                pose,
                PoseLandmark.LEFT_SHOULDER,
                PoseLandmark.LEFT_HIP,
                PoseLandmark.LEFT_ANKLE
        );

        double rightBackAngle = get3DAngle(
                pose,
                PoseLandmark.RIGHT_SHOULDER,
                PoseLandmark.RIGHT_HIP,
                PoseLandmark.RIGHT_ANKLE
        );

        return (leftBackAngle + rightBackAngle) / 2.0;
    }
}