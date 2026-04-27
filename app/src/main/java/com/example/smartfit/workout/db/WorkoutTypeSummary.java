package com.example.smartfit.workout.db;

// Room projection — not an @Entity, just a query result shape.
public class WorkoutTypeSummary {
    public String workoutType;
    public int repCount;
    public long activeDurationMillis;
}