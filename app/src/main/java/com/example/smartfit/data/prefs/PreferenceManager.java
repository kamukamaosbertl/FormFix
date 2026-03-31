package com.example.smartfit.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smartfit.data.model.UserProfile;

public class PreferenceManager {

    private static final String PREFS_NAME = "smartfit_prefs";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_STEP_GOAL = "step_goal";
    private static final String KEY_WATER_GOAL = "water_goal";

    private final SharedPreferences preferences;

    public PreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public UserProfile getUserProfile() {
        String name = preferences.getString(KEY_NAME, "").trim();
        int stepGoal = preferences.getInt(KEY_STEP_GOAL, 8000);
        float waterGoal = preferences.getFloat(KEY_WATER_GOAL, 2.0f);

        return new UserProfile(name, stepGoal, waterGoal);
    }

    public void saveUserProfile(String name, int stepGoal, float waterGoal) {
        preferences.edit()
                .putString(KEY_NAME, name)
                .putInt(KEY_STEP_GOAL, stepGoal)
                .putFloat(KEY_WATER_GOAL, waterGoal)
                .apply();
    }
}