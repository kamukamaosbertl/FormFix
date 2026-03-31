package com.example.smartfit.data.repository;

import com.example.smartfit.R;
import com.example.smartfit.data.model.DashboardItem;
import com.example.smartfit.device.DeviceDashboardActivity;
import com.example.smartfit.device.SensorMonitorActivity;
import com.example.smartfit.device.SystemMonitorActivity;
import com.example.smartfit.ui.history.HistoryActivity;
import com.example.smartfit.ui.maps.MapActivity;
import com.example.smartfit.ui.notifications.NotificationActivity;
import com.example.smartfit.ui.workout.WorkoutActivity;

import java.util.ArrayList;
import java.util.List;

public class DashboardRepository {

    public List<DashboardItem> getMainFeatures() {
        List<DashboardItem> items = new ArrayList<>();

        items.add(new DashboardItem(
                R.drawable.ic_fitness,
                "Start Workout",
                "Open camera and begin smart posture tracking",
                WorkoutActivity.class
        ));

        items.add(new DashboardItem(
                R.drawable.ic_history,
                "Workout History",
                "Review past sessions and progress",
                HistoryActivity.class
        ));

        items.add(new DashboardItem(
                R.drawable.ic_map,
                "Map Module",
                "Access location and workout map features",
                MapActivity.class
        ));

        items.add(new DashboardItem(
                R.drawable.ic_notifications,
                "Notifications",
                "Manage reminders and workout alerts",
                NotificationActivity.class
        ));

        return items;
    }

    public List<DashboardItem> getProjectModules() {
        List<DashboardItem> items = new ArrayList<>();

        items.add(new DashboardItem(
                R.drawable.ic_phone_android,
                "Device Info",
                "View device and hardware specifications",
                DeviceDashboardActivity.class
        ));

        return items;
    }
}