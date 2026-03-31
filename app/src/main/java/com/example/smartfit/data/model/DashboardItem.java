package com.example.smartfit.data.model;

public class DashboardItem {

    private final int iconResId;
    private final String title;
    private final String subtitle;
    private final Class<?> destination;

    public DashboardItem(int iconResId, String title, String subtitle, Class<?> destination) {
        this.iconResId = iconResId;
        this.title = title;
        this.subtitle = subtitle;
        this.destination = destination;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Class<?> getDestination() {
        return destination;
    }
}