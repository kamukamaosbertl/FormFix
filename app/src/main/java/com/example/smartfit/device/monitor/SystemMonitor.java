package com.example.smartfit.device.monitor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.smartfit.device.collectors.BatteryCollector;
import com.example.smartfit.device.collectors.CpuCollector;
import com.example.smartfit.device.collectors.MemoryCollector;
import com.example.smartfit.device.collectors.StorageCollector;
import com.example.smartfit.device.model.BatterySnapshot;
import com.example.smartfit.device.model.CpuSnapshot;
import com.example.smartfit.device.model.MemorySnapshot;
import com.example.smartfit.device.model.StorageSnapshot;

public class SystemMonitor {

    public interface Listener {
        void onUpdate(
                CpuSnapshot cpu,
                MemorySnapshot memory,
                StorageSnapshot storage,
                BatterySnapshot battery
        );
    }

    private final Context context;
    private final Listener listener;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final CpuCollector cpuCollector = new CpuCollector();

    private boolean running = false;

    private final Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {

            if (!running) return;

            CpuSnapshot cpu = cpuCollector.collect();
            MemorySnapshot memory = MemoryCollector.collect(context);
            StorageSnapshot storage = StorageCollector.collect();
            BatterySnapshot battery = BatteryCollector.collect(context);

            listener.onUpdate(cpu, memory, storage, battery);

            handler.postDelayed(this, 1000); // run every 1 second
        }
    };

    public SystemMonitor(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void start() {
        if (running) return;
        running = true;
        handler.post(monitorRunnable);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(monitorRunnable);
    }
}