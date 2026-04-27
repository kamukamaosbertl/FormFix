package com.example.smartfit.workout.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// One row per completed or cancelled workout session.
// Mirrors WorkoutSession but is flat — no nested objects — so Room can store it.
@Entity(tableName = "workout_sessions")
public class WorkoutSessionEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String workoutType;      // WorkoutType.name()
    public String state;            // WorkoutState.name()

    public long startTimeMillis;
    public long endTimeMillis;

    // Metrics
    public int repCount;
    public long activeDurationMillis;
    public long holdDurationMillis;

    // Human-readable summary stored at save time
    public String lastFeedback;
}