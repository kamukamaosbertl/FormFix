package com.example.smartfit.workout.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkoutDao {

    @Insert
    void insert(WorkoutSessionEntity session);

    // All sessions newest-first — used by the history screen
    @Query("SELECT * FROM workout_sessions ORDER BY startTimeMillis DESC")
    List<WorkoutSessionEntity> getAllSessions();

    // Aggregate rep count per workout type — used for the bar chart
    @Query("SELECT workoutType, SUM(repCount) as repCount, " +
            "SUM(activeDurationMillis) as activeDurationMillis " +
            "FROM workout_sessions GROUP BY workoutType")
    List<WorkoutTypeSummary> getSummaryByType();

    @Query("DELETE FROM workout_sessions")
    void clearAll();
}