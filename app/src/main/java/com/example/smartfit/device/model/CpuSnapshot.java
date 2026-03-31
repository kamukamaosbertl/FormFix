package com.example.smartfit.device.model;

public class CpuSnapshot {

    private final float usagePercent;

    public CpuSnapshot(float usagePercent) {

        // clamp value between 0 and 100
        if (usagePercent < 0) usagePercent = 0;
        if (usagePercent > 100) usagePercent = 100;

        this.usagePercent = usagePercent;
    }

    public float getUsagePercent() {
        return usagePercent;
    }

    public int getRoundedUsage(){
        return Math.round(usagePercent);
    }
}