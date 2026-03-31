package com.example.smartfit.notifications;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    public static final String DAILY_WORK_NAME = "daily_workout_reminder";
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_MESSAGE = "key_message";

    public static void scheduleDailyReminder(
            Context context,
            String title,
            String message,
            int hourOfDay,
            int minute
    ) {
        Data inputData = new Data.Builder()
                .putString(KEY_TITLE, title)
                .putString(KEY_MESSAGE, message)
                .build();

        long initialDelay = calculateInitialDelay(hourOfDay, minute);

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        WorkoutReminderWorker.class,
                        24,
                        TimeUnit.HOURS
                )
                        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                DAILY_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }

    public static void cancelDailyReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_WORK_NAME);
    }

    public static void triggerTestNotification(Context context, String title, String message) {
        Data inputData = new Data.Builder()
                .putString(KEY_TITLE, title)
                .putString(KEY_MESSAGE, message)
                .build();

        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(WorkoutReminderWorker.class)
                        .setInputData(inputData)
                        .build();

        WorkManager.getInstance(context).enqueue(request);
    }

    private static long calculateInitialDelay(int hourOfDay, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar nextRun = Calendar.getInstance();

        nextRun.set(Calendar.HOUR_OF_DAY, hourOfDay);
        nextRun.set(Calendar.MINUTE, minute);
        nextRun.set(Calendar.SECOND, 0);
        nextRun.set(Calendar.MILLISECOND, 0);

        if (nextRun.before(now) || nextRun.equals(now)) {
            nextRun.add(Calendar.DAY_OF_YEAR, 1);
        }

        return nextRun.getTimeInMillis() - now.getTimeInMillis();
    }
}