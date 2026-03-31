package com.example.smartfit.workout.repository;

import com.example.smartfit.workout.model.WorkoutSession;

import java.util.ArrayList;
import java.util.List;

public class WorkoutRepository {

    private static WorkoutRepository instance;
    private final List<WorkoutSession> sessions = new ArrayList<>();

    private WorkoutRepository() {
    }

    public static synchronized WorkoutRepository getInstance() {
        if (instance == null) {
            instance = new WorkoutRepository();
        }
        return instance;
    }

    public void saveSession(WorkoutSession session) {
        sessions.add(session);
    }

    public List<WorkoutSession> getAllSessions() {
        return new ArrayList<>(sessions);
    }

    public WorkoutSession getLatestSession() {
        if (sessions.isEmpty()) {
            return null;
        }
        return sessions.get(sessions.size() - 1);
    }

    public void clearAll() {
        sessions.clear();
    }
}