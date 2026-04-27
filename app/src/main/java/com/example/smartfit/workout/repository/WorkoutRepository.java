package com.example.smartfit.workout.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.example.smartfit.workout.db.WorkoutDao;
import com.example.smartfit.workout.db.WorkoutDatabase;
import com.example.smartfit.workout.db.WorkoutSessionEntity;
import com.example.smartfit.workout.db.WorkoutTypeSummary;
import com.example.smartfit.workout.model.WorkoutSession;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutRepository {

    private static WorkoutRepository instance;

    private final WorkoutDao dao;

    // Single background thread for all DB writes — keeps the main thread clean.
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private WorkoutRepository(@NonNull Context context) {
        dao = WorkoutDatabase.getInstance(context).workoutDao();
    }

    public static synchronized WorkoutRepository getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new WorkoutRepository(context.getApplicationContext());
        }
        return instance;
    }

    // Called from WorkoutSessionManager — fire-and-forget write on background thread.
    public void saveSession(@NonNull WorkoutSession session) {
        dbExecutor.execute(() -> dao.insert(toEntity(session)));
    }

    // Called from history screen — must be called off the main thread.
    @WorkerThread
    public List<WorkoutSessionEntity> getAllSessions() {
        return dao.getAllSessions();
    }

    // Called from chart — must be called off the main thread.
    @WorkerThread
    public List<WorkoutTypeSummary> getSummaryByType() {
        return dao.getSummaryByType();
    }

    // Maps domain model → Room entity at save time.
    // Keeps the domain model free of Room annotations.
    @NonNull
    private WorkoutSessionEntity toEntity(@NonNull WorkoutSession session) {
        WorkoutSessionEntity e = new WorkoutSessionEntity();
        e.workoutType        = session.getWorkoutType().name();
        e.state              = session.getState().name();
        e.startTimeMillis    = session.getStartTimeMillis();
        e.endTimeMillis      = session.getEndTimeMillis();
        e.repCount           = session.getRepCount();
        e.activeDurationMillis = session.getMetrics().getActiveDurationMillis();
        e.holdDurationMillis = session.getHoldDurationMillis();
        e.lastFeedback       = session.getLastFeedback();
        return e;
    }
}