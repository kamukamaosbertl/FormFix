package com.example.smartfit.data.model;

public class GoalSummary {
    private final String welcomeText;
    private final String goalText;
    private final String motivationText;

    public GoalSummary(String welcomeText, String goalText, String motivationText) {
        this.welcomeText = welcomeText;
        this.goalText = goalText;
        this.motivationText = motivationText;
    }

    public String getWelcomeText() {
        return welcomeText;
    }

    public String getGoalText() {
        return goalText;
    }

    public String getMotivationText() {
        return motivationText;
    }
}