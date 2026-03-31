package com.example.smartfit.notifications;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WorkoutReminderWorker extends Worker {

    public WorkoutReminderWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams
    ) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        NotificationHelper.createNotificationChannel(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                return Result.success();
            }
        }

        String title = getInputData().getString(ReminderScheduler.KEY_TITLE);
        String message = getInputData().getString(ReminderScheduler.KEY_MESSAGE);

        if (title == null || title.trim().isEmpty()) {
            title = "SmartFit Reminder";
        }

        if (message == null || message.trim().isEmpty()) {
            message = "Time for your workout. Open SmartFit and train smart today.";
        }

        NotificationHelper.showNotification(context, 1001, title, message);
        return Result.success();
    }
}