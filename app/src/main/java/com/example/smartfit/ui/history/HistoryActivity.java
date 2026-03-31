package com.example.smartfit.ui.history;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfit.R;
import com.example.smartfit.workout.model.WorkoutSession;
import com.example.smartfit.workout.repository.WorkoutRepository;

import java.util.List;
import java.util.Collections;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerWorkoutHistory;
    private TextView tvEmptyHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerWorkoutHistory = findViewById(R.id.recycler_workout_history);
        tvEmptyHistory = findViewById(R.id.tv_empty_history);

        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        List<WorkoutSession> sessions = WorkoutRepository.getInstance().getAllSessions();

        if (sessions.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            recyclerWorkoutHistory.setVisibility(View.GONE);
            return;
        }

       Collections.reverse(sessions);

        tvEmptyHistory.setVisibility(View.GONE);
        recyclerWorkoutHistory.setVisibility(View.VISIBLE);

        recyclerWorkoutHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerWorkoutHistory.setAdapter(new WorkoutHistoryAdapter(sessions));
    }
}