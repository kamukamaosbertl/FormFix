package com.example.smartfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "smartfit_prefs";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_STEP_GOAL = "step_goal";
    private static final String KEY_WATER_GOAL = "water_goal";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_REMINDERS = "reminders";
    private static final String KEY_UNITS = "units";

    private TextInputEditText etName;
    private TextInputEditText etStepGoal;
    private TextInputEditText etWaterGoal;
    private SwitchMaterial switchNotifications;
    private SwitchMaterial switchReminders;
    private RadioGroup rgUnits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        etName = findViewById(R.id.et_name);
        etStepGoal = findViewById(R.id.et_step_goal);
        etWaterGoal = findViewById(R.id.et_water_goal);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchReminders = findViewById(R.id.switch_reminders);
        rgUnits = findViewById(R.id.rg_units);

        MaterialButton btnSave = findViewById(R.id.btn_save_settings);
        MaterialButton btnReset = findViewById(R.id.btn_reset_form);
        MaterialButton btnAbout = findViewById(R.id.btn_about);
        MaterialButton btnBackHome = findViewById(R.id.btn_back_home);
        MaterialButton btnLogout = findViewById(R.id.btn_logout);

        loadSettings();

        btnSave.setOnClickListener(v -> saveSettings());
        btnReset.setOnClickListener(v -> resetForm(true));
        btnAbout.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, AboutActivity.class)));
        btnBackHome.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        etName.setText(prefs.getString(KEY_NAME, ""));
        etStepGoal.setText(String.valueOf(prefs.getInt(KEY_STEP_GOAL, 8000)));
        etWaterGoal.setText(String.valueOf(prefs.getFloat(KEY_WATER_GOAL, 2.0f)));
        switchNotifications.setChecked(prefs.getBoolean(KEY_NOTIFICATIONS, true));
        switchReminders.setChecked(prefs.getBoolean(KEY_REMINDERS, true));

        String units = prefs.getString(KEY_UNITS, "metric");
        if ("imperial".equals(units)) {
            rgUnits.check(R.id.rb_imperial);
        } else {
            rgUnits.check(R.id.rb_metric);
        }
    }

    private void saveSettings() {
        String name = getText(etName);
        String stepGoalText = getText(etStepGoal);
        String waterGoalText = getText(etWaterGoal);

        int stepGoal;
        float waterGoal;

        try {
            stepGoal = Integer.parseInt(stepGoalText);
            waterGoal = Float.parseFloat(waterGoalText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.settings_toast_invalid_values, Toast.LENGTH_SHORT).show();
            return;
        }

        if (stepGoal <= 0 || waterGoal <= 0f) {
            Toast.makeText(this, R.string.settings_toast_positive_values, Toast.LENGTH_SHORT).show();
            return;
        }

        String units = rgUnits.getCheckedRadioButtonId() == R.id.rb_imperial ? "imperial" : "metric";

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_NAME, name)
                .putInt(KEY_STEP_GOAL, stepGoal)
                .putFloat(KEY_WATER_GOAL, waterGoal)
                .putBoolean(KEY_NOTIFICATIONS, switchNotifications.isChecked())
                .putBoolean(KEY_REMINDERS, switchReminders.isChecked())
                .putString(KEY_UNITS, units)
                .apply();

        Toast.makeText(this, R.string.settings_toast_saved, Toast.LENGTH_SHORT).show();
    }

    private void resetForm(boolean showToast) {
        etName.setText("");
        etStepGoal.setText("8000");
        etWaterGoal.setText("2.0");
        switchNotifications.setChecked(true);
        switchReminders.setChecked(true);
        rgUnits.check(R.id.rb_metric);

        if (showToast) {
            Toast.makeText(this, R.string.settings_toast_form_reset, Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();
        resetForm(false);
        Toast.makeText(this, R.string.settings_toast_logged_out, Toast.LENGTH_SHORT).show();
    }

    private String getText(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
