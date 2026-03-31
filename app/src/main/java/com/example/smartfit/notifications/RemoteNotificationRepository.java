package com.example.smartfit.notifications;

import android.os.Handler;
import android.os.Looper;

public class RemoteNotificationRepository {

    public interface Callback {
        void onSuccess();
        void onError(String message);
    }

    public void sendNotification(NotificationPayload payload, Callback callback) {
        // Replace this mock with Retrofit / OkHttp / Firebase Callable Function later.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (payload.getTitle() == null || payload.getTitle().trim().isEmpty()) {
                callback.onError("Title is required");
                return;
            }

            callback.onSuccess();
        }, 700);
    }
}