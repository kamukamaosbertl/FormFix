package com.example.smartfit.device;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfit.R;
import com.example.smartfit.device.adapter.DeviceCategoryAdapter;
import com.example.smartfit.device.collectors.StaticDeviceInfoCollector;
import com.example.smartfit.device.model.InfoCategory;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DeviceDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerDeviceCategories;

    private MaterialButton btnSystemMonitor;
    private MaterialButton btnSensorMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_dashboard);

        recyclerDeviceCategories = findViewById(R.id.recycler_device_categories);

        btnSystemMonitor = findViewById(R.id.btn_system_monitor);
        btnSensorMonitor = findViewById(R.id.btn_sensor_monitor);

        setupRecyclerView();
        setupButtons();
    }

    private void setupRecyclerView() {

        List<InfoCategory> categories = StaticDeviceInfoCollector.collect(this);

        recyclerDeviceCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerDeviceCategories.setAdapter(new DeviceCategoryAdapter(categories));
    }

    private void setupButtons() {

        btnSystemMonitor.setOnClickListener(v ->
                startActivity(new Intent(this, SystemMonitorActivity.class)));

        btnSensorMonitor.setOnClickListener(v ->
                startActivity(new Intent(this, SensorMonitorActivity.class)));

    }
}