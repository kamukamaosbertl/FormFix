package com.example.smartfit.data.repository;

import android.content.Context;

import com.example.smartfit.data.model.GoalSummary;
import com.example.smartfit.data.model.UserProfile;
import com.example.smartfit.data.prefs.PreferenceManager;

public class UserRepository {

    private final PreferenceManager preferenceManager;

    public UserRepository(Context context) {
        this.preferenceManager = new PreferenceManager(context);
    }

    public UserProfile getUserProfile() {
        return preferenceManager.getUserProfile();
    }

    public GoalSummary getGoalSummary() {
        UserProfile user = getUserProfile();

        String welcome;
        String motivation;

        if (user.getName().isEmpty()) {
            welcome = "Welcome to SmartFit";
            motivation = "Train smart. Track posture. Improve your form.";
        } else {
            welcome = "Welcome back, " + user.getName();
            motivation = "Ready for today’s smart workout session?";
        }

        String goals = "Goals: " + user.getStepGoal() + " steps • " + user.getWaterGoal() + "L water";

        return new GoalSummary(welcome, goals, motivation);
    }
}