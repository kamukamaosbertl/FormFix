package com.example.smartfit.data.model;

public class UserProfile {
    private final String name;
    private final int stepGoal;
    private final float waterGoal;

    public UserProfile(String name, int stepGoal, float waterGoal) {
        this.name = name;
        this.stepGoal = stepGoal;
        this.waterGoal = waterGoal;
    }

    public String getName() {
        return name;
    }

    public int getStepGoal() {
        return stepGoal;
    }

    public float getWaterGoal() {
        return waterGoal;
    }
}