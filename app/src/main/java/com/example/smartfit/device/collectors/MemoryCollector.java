package com.example.smartfit.device.collectors;

import android.app.ActivityManager;
import android.content.Context;

import com.example.smartfit.device.model.MemorySnapshot;

public class MemoryCollector {

    public static MemorySnapshot collect(Context context) {

        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long total = memoryInfo.totalMem;
        long available = memoryInfo.availMem;

        return new MemorySnapshot(total, available);
    }
}