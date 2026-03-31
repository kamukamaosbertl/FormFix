package com.example.smartfit.notifications;

public class NotificationPayload {

    private final String title;
    private final String message;
    private final String targetType;
    private final String targetValue;

    public NotificationPayload(String title, String message, String targetType, String targetValue) {
        this.title = title;
        this.message = message;
        this.targetType = targetType;
        this.targetValue = targetValue;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetValue() {
        return targetValue;
    }
}