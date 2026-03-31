package com.example.smartfit.device.model;

public class BatterySnapshot{
    private final int percent; // 0..100, or -1 unkwnown
    private final boolean isCharging;

    private final int temp;
    private final long voltage;

    public BatterySnapshot(int percent, boolean isCharging, int temp, long voltage){
        this.percent = percent;
        this.isCharging = isCharging;
        this.temp = temp;
        this.voltage = voltage;
    }

    public int getPercent() {
        return percent;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public int getTemperature(){
        return temp;
    }

    public long getVoltage(){
        return voltage;
    }
}