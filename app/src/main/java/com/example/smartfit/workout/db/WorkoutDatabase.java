package com.example.smartfit.workout.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {WorkoutSessionEntity.class}, version = 1, exportSchema = false)
public abstract class WorkoutDatabase extends RoomDatabase {

    private static volatile WorkoutDatabase instance;

    public abstract WorkoutDao workoutDao();

    public static WorkoutDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (WorkoutDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    WorkoutDatabase.class,
                                    "smartfit_workouts.db"
                            )
                            // Allow main-thread queries during development only —
                            // switch to background executor before release.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}