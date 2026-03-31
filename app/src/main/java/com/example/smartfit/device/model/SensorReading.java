package com.example.smartfit.device.model;

public class SensorReading {

    private final int sensorType;
    private final float x;
    private final float y;
    private final float z;
    private final int valueCount;

    public SensorReading(int sensorType, float x, float y, float z, int valueCount) {
        this.sensorType = sensorType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.valueCount = valueCount;
    }

    public int getSensorType() {
        return sensorType;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public int getValueCount() {
        return valueCount;
    }

    public boolean isSingleValue() {
        return valueCount <= 1;
    }
}