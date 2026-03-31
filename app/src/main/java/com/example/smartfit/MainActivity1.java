//package com.example.smartfit;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.smartfit.device.DeviceDashboardActivity;
//import com.example.smartfit.device.SystemMonitorActivity;
//import com.example.smartfit.device.SensorMonitorActivity;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//
//public class MainActivity1 extends AppCompatActivity {
//
//    private static final String PREFS_NAME = "smartfit_prefs";
//    private static final String KEY_NAME = "user_name";
//    private static final String KEY_STEP_GOAL = "step_goal";
//    private static final String KEY_WATER_GOAL = "water_goal";
//
//    private FloatingActionButton fabMain;
//    private FloatingActionButton fabSettings;
//    private FloatingActionButton fabAbout;
//    private boolean isOpen = false;
//
//    private TextView tvWelcome;
//    private TextView tvGoalSummary;
//    private TextView tvMotivation;
//
//    private MaterialButton btnStartWorkout;
//    private MaterialButton btnDeviceDashboard;
//    private MaterialButton btnSystemMonitor;
//    private MaterialButton btnSensorMonitor;
//    private MaterialButton btnMapModule;
//    private MaterialButton btnNotifications;
//    private MaterialButton btnWorkoutHistory;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        tvWelcome = findViewById(R.id.tv_welcome);
//        tvGoalSummary = findViewById(R.id.tv_goal_summary);
//        tvMotivation = findViewById(R.id.tv_motivation);
//
//        fabMain = findViewById(R.id.fab_main);
//        fabSettings = findViewById(R.id.fab_settings);
//        fabAbout = findViewById(R.id.fab_about);
//
//        btnStartWorkout = findViewById(R.id.btn_start_workout);
//        btnDeviceDashboard = findViewById(R.id.btn_device_dashboard);
//        btnSystemMonitor = findViewById(R.id.btn_system_monitor);
//        btnSensorMonitor = findViewById(R.id.btn_sensor_monitor);
//        btnMapModule = findViewById(R.id.btn_map_module);
//        btnNotifications = findViewById(R.id.btn_notifications);
//        btnWorkoutHistory = findViewById(R.id.btn_workout_history);
//
//        fabMain.setOnClickListener(v -> toggleFab());
//
//        View.OnClickListener openSettings =
//                v -> startActivity(new Intent(MainActivity1.this, SettingsActivity.class));
//
//        View.OnClickListener openAbout =
//                v -> startActivity(new Intent(MainActivity1.this, AboutActivity.class));
//
//        fabSettings.setOnClickListener(openSettings);
//        fabAbout.setOnClickListener(openAbout);
//
//        btnStartWorkout.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity1.this, WorkoutActivity.class)));
//
//        btnDeviceDashboard.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity1.this, DeviceDashboardActivity.class)));
//
//        btnSystemMonitor.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity1.this, SystemMonitorActivity.class)));
//
//        btnSensorMonitor.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity1.this, SensorMonitorActivity.class)));
//
//        btnMapModule.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity1.this, MapActivity.class)));
//
//        btnNotifications.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity1.this, NotificationActivity.class)));
//
//        btnWorkoutHistory.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity1.this, HistoryActivity.class)));
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        refreshDashboard();
//    }
//
//    private void refreshDashboard() {
//        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//
//        String name = prefs.getString(KEY_NAME, "").trim();
//        int stepGoal = prefs.getInt(KEY_STEP_GOAL, 8000);
//        float waterGoal = prefs.getFloat(KEY_WATER_GOAL, 2.0f);
//
//        if (name.isEmpty()) {
//            tvWelcome.setText("Welcome to SmartFit");
//            tvMotivation.setText("Train smart. Track posture. Improve your form.");
//        } else {
//            tvWelcome.setText("Welcome back, " + name);
//            tvMotivation.setText("Ready for today’s smart workout session?");
//        }
//
//        tvGoalSummary.setText("Goals: " + stepGoal + " steps • " + waterGoal + "L water");
//    }
//
//    private void toggleFab() {
//        if (isOpen) {
//            fabSettings.setVisibility(View.GONE);
//            fabAbout.setVisibility(View.GONE);
//        } else {
//            fabSettings.setVisibility(View.VISIBLE);
//            fabAbout.setVisibility(View.VISIBLE);
//        }
//        isOpen = !isOpen;
//    }
//}