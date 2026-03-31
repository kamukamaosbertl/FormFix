package com.example.smartfit.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfit.AboutActivity;
import com.example.smartfit.R;
import com.example.smartfit.SettingsActivity;
import com.example.smartfit.data.model.DashboardItem;
import com.example.smartfit.data.model.GoalSummary;
import com.example.smartfit.data.repository.DashboardRepository;
import com.example.smartfit.data.repository.UserRepository;
import com.example.smartfit.notifications.NotificationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabMain;
    private FloatingActionButton fabSettings;
    private FloatingActionButton fabAbout;
    private boolean isOpen = false;

    private TextView tvWelcome;
    private TextView tvGoalSummary;
    private TextView tvMotivation;

    private RecyclerView recyclerMainFeatures;
    private RecyclerView recyclerProjectModules;

    private UserRepository userRepository;
    private DashboardRepository dashboardRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.createNotificationChannel(this);

        userRepository = new UserRepository(this);
        dashboardRepository = new DashboardRepository();

        tvWelcome = findViewById(R.id.tv_welcome);
        tvGoalSummary = findViewById(R.id.tv_goal_summary);
        tvMotivation = findViewById(R.id.tv_motivation);

        recyclerMainFeatures = findViewById(R.id.recycler_main_features);
        recyclerProjectModules = findViewById(R.id.recycler_project_modules);

        fabMain = findViewById(R.id.fab_main);
        fabSettings = findViewById(R.id.fab_settings);
        fabAbout = findViewById(R.id.fab_about);

        setupRecyclers();
        setupFabs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();
    }

    private void setupRecyclers() {
        List<DashboardItem> mainItems = dashboardRepository.getMainFeatures();
        List<DashboardItem> moduleItems = dashboardRepository.getProjectModules();

        DashboardAdapter mainAdapter = new DashboardAdapter(mainItems, this::openDashboardDestination);
        DashboardAdapter moduleAdapter = new DashboardAdapter(moduleItems, this::openDashboardDestination);

        recyclerMainFeatures.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerMainFeatures.setAdapter(mainAdapter);

        recyclerProjectModules.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProjectModules.setAdapter(moduleAdapter);
    }

    private void setupFabs() {
        fabMain.setOnClickListener(v -> toggleFab());

        fabSettings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        fabAbout.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AboutActivity.class)));
    }

    private void refreshDashboard() {
        GoalSummary summary = userRepository.getGoalSummary();

        tvWelcome.setText(summary.getWelcomeText());
        tvGoalSummary.setText(summary.getGoalText());
        tvMotivation.setText(summary.getMotivationText());
    }

    private void openDashboardDestination(DashboardItem item) {
        startActivity(new Intent(this, item.getDestination()));
    }

    private void toggleFab() {
        if (isOpen) {
            fabSettings.setVisibility(View.GONE);
            fabAbout.setVisibility(View.GONE);
        } else {
            fabSettings.setVisibility(View.VISIBLE);
            fabAbout.setVisibility(View.VISIBLE);
        }
        isOpen = !isOpen;
    }
}