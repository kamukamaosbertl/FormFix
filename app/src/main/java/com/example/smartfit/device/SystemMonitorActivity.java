package com.example.smartfit.device;

import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfit.R;
import com.example.smartfit.device.collectors.NetworkCollector;
import com.example.smartfit.device.model.NetworkSnapshot;
import com.example.smartfit.device.monitor.ConnectivityLiveMonitor;
import com.example.smartfit.device.monitor.SystemMonitor;
import com.example.smartfit.device.receiver.NetworkStateReceiver;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class SystemMonitorActivity extends AppCompatActivity {

    private CircularProgressIndicator cpuBar;
    private CircularProgressIndicator memoryBar;
    private CircularProgressIndicator storageBar;
    private CircularProgressIndicator batteryBar;

    private TextView tvCpuPercent, tvCpuCores, tvCpuLoad;
    private TextView tvMemoryPercent, tvMemoryUsed, tvMemoryTotal;
    private TextView tvStoragePercent, tvStorageUsed, tvStorageFree;
    private TextView tvBatteryPercent, tvBatteryState, tvBatteryTemp;
    private TextView tvBatteryVoltage;

    private TextView tvNetworkStatus, tvNetworkType, tvNetworkSsid, tvNetworkSpeed, tvNetworkIp;
    private TextView tvSystemEvent;

    private SystemMonitor systemMonitor;
    private ConnectivityLiveMonitor connectivityLiveMonitor;
    private NetworkStateReceiver networkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_monitor);

        cpuBar = findViewById(R.id.progress_cpu);
        memoryBar = findViewById(R.id.progress_memory);
        storageBar = findViewById(R.id.progress_storage);
        batteryBar = findViewById(R.id.progress_battery);

        tvCpuPercent = findViewById(R.id.tv_cpu_percent);
        tvCpuCores = findViewById(R.id.tv_cpu_cores);
        tvCpuLoad = findViewById(R.id.tv_cpu_load);
        tvMemoryPercent = findViewById(R.id.tv_memory_percent);
        tvMemoryUsed = findViewById(R.id.tv_memory_used);
        tvMemoryTotal = findViewById(R.id.tv_memory_total);
        tvStoragePercent = findViewById(R.id.tv_storage_percent);
        tvStorageUsed = findViewById(R.id.tv_storage_used);
        tvStorageFree = findViewById(R.id.tv_storage_free);
        tvBatteryPercent = findViewById(R.id.tv_battery_percent);
        tvBatteryState = findViewById(R.id.tv_battery_state);
        tvBatteryTemp = findViewById(R.id.tv_battery_temp);
        tvBatteryVoltage = findViewById(R.id.tv_battery_voltage);

        tvNetworkStatus = findViewById(R.id.tv_network_status);
        tvNetworkType = findViewById(R.id.tv_network_type);
        tvNetworkSsid = findViewById(R.id.tv_network_ssid);
        tvNetworkSpeed = findViewById(R.id.tv_network_speed);
        tvNetworkIp = findViewById(R.id.tv_network_ip);
        tvSystemEvent = findViewById(R.id.tv_system_event);

        systemMonitor = new SystemMonitor(this, (cpu, memory, storage, battery) -> {

            int cpuPercent = (int) cpu.getUsagePercent();
            cpuBar.setProgress(cpuPercent);
            tvCpuPercent.setText(cpuPercent + " %");
            tvCpuCores.setText("Cores: " + Runtime.getRuntime().availableProcessors());
            tvCpuLoad.setText(String.format("Load: %.2f", cpu.getUsagePercent() / 100));

            memoryBar.setProgress(memory.getUsedPercent());
            tvMemoryPercent.setText(memory.getUsedPercent() + " %");
            long usedMB = memory.getUsed() / (1024 * 1024);
            long totalMB = memory.getTotal() / (1024 * 1024);
            tvMemoryUsed.setText("Used: " + usedMB + " MB");
            tvMemoryTotal.setText("Total: " + totalMB + " MB");

            storageBar.setProgress(storage.getUsedPercent());
            tvStoragePercent.setText(storage.getUsedPercent() + " %");
            long usedGB = storage.getUsed() / (1024 * 1024 * 1024);
            long freeGB = storage.getAvailable() / (1024 * 1024 * 1024);
            tvStorageUsed.setText("Used: " + usedGB + " GB");
            tvStorageFree.setText("Free: " + freeGB + " GB");

            batteryBar.setProgress(battery.getPercent());
            tvBatteryPercent.setText(battery.getPercent() + " %");
            tvBatteryState.setText(battery.isCharging() ? "Charging" : "Not charging");
            tvBatteryTemp.setText("Temp: " + battery.getTemperature() + " °C");
            tvBatteryVoltage.setText("Voltage: " + (battery.getVoltage() / 1000f) + " V");
        });

        connectivityLiveMonitor = new ConnectivityLiveMonitor(this, this::updateNetworkUI);

        networkStateReceiver = new NetworkStateReceiver(eventMessage ->
                runOnUiThread(() -> {
                    tvSystemEvent.setText(eventMessage);
                    Toast.makeText(this, eventMessage, Toast.LENGTH_SHORT).show();
                }));

        updateNetworkUI(NetworkCollector.collect(this));
    }

    private void updateNetworkUI(NetworkSnapshot network) {
        runOnUiThread(() -> {
            tvNetworkStatus.setText(network.connected ? "🟢 Connected" : "🔴 Disconnected");
            tvNetworkType.setText("Type: " + network.networkType);
            tvNetworkSsid.setText("SSID: " + (network.ssid == null || network.ssid.isEmpty() ? "--" : network.ssid));
            tvNetworkSpeed.setText("Speed: " + (network.linkSpeed > 0 ? network.linkSpeed + " Mbps" : "--"));
            tvNetworkIp.setText("IP: " + (network.ipAddress == null || network.ipAddress.isEmpty() ? "--" : network.ipAddress));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        systemMonitor.start();
        connectivityLiveMonitor.start();

        IntentFilter filter = new IntentFilter();
        filter.addAction(android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED);
        filter.addAction(android.content.Intent.ACTION_POWER_CONNECTED);
        filter.addAction(android.content.Intent.ACTION_POWER_DISCONNECTED);

        registerReceiver(networkStateReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        systemMonitor.stop();
        connectivityLiveMonitor.stop();

        try {
            unregisterReceiver(networkStateReceiver);
        } catch (Exception ignored) {
        }
    }
}