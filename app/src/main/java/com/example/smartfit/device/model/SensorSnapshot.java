package com.example.smartfit.device.model;

public class SensorSnapshot {

    private final int sensorType;
    private final String title;
    private final String subtitle;
    private final String emoji;

    public SensorSnapshot(int sensorType, String title, String subtitle, String emoji) {
        this.sensorType = sensorType;
        this.title = title;
        this.subtitle = subtitle;
        this.emoji = emoji;
    }

    public int getSensorType() {
        return sensorType;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getEmoji() {
        return emoji;
    }
}