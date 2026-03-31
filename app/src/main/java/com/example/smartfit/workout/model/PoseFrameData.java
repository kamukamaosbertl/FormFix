package com.example.smartfit.workout.model;

public class PoseFrameData {

    private final double leftElbowAngle;
    private final double rightElbowAngle;
    private final double leftKneeAngle;
    private final double rightKneeAngle;
    private final double leftHipAngle;
    private final double rightHipAngle;
    private final double backAngle;

    public PoseFrameData(
            double leftElbowAngle,
            double rightElbowAngle,
            double leftKneeAngle,
            double rightKneeAngle,
            double leftHipAngle,
            double rightHipAngle,
            double backAngle
    ) {
        this.leftElbowAngle = leftElbowAngle;
        this.rightElbowAngle = rightElbowAngle;
        this.leftKneeAngle = leftKneeAngle;
        this.rightKneeAngle = rightKneeAngle;
        this.leftHipAngle = leftHipAngle;
        this.rightHipAngle = rightHipAngle;
        this.backAngle = backAngle;
    }

    public double getLeftElbowAngle() {
        return leftElbowAngle;
    }

    public double getRightElbowAngle() {
        return rightElbowAngle;
    }

    public double getLeftKneeAngle() {
        return leftKneeAngle;
    }

    public double getRightKneeAngle() {
        return rightKneeAngle;
    }

    public double getLeftHipAngle() {
        return leftHipAngle;
    }

    public double getRightHipAngle() {
        return rightHipAngle;
    }

    public double getBackAngle() {
        return backAngle;
    }

    public double getAverageElbowAngle() {
        return (leftElbowAngle + rightElbowAngle) / 2.0;
    }

    public double getAverageKneeAngle() {
        return (leftKneeAngle + rightKneeAngle) / 2.0;
    }

    public double getAverageHipAngle() {
        return (leftHipAngle + rightHipAngle) / 2.0;
    }
}