package com.example.smartfit.ui.history;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfit.R;
import com.example.smartfit.workout.db.WorkoutSessionEntity;
import com.example.smartfit.workout.export.WorkoutPdfExporter;
import com.example.smartfit.workout.model.WorkoutSession;
import com.example.smartfit.workout.repository.WorkoutRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerWorkoutHistory;
    private TextView tvEmptyHistory;
    private final ExecutorService exportExecutor = Executors.newSingleThreadExecutor();

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
        List<WorkoutSessionEntity> sessions =
                WorkoutRepository.getInstance(this).getAllSessions();

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

    private void exportToPdf() {
        exportExecutor.execute(() -> {
            try {
                List<WorkoutSessionEntity> sessions =
                        WorkoutRepository.getInstance(this).getAllSessions();

                WorkoutPdfExporter exporter = new WorkoutPdfExporter();
                File pdf = exporter.export(this, sessions);

                runOnUiThread(() -> {
                    // Share the file via intent so the user can open or send it
                    Uri uri = FileProvider.getUriForFile(this,
                            getPackageName() + ".fileprovider", pdf);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "application/pdf");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Open workout history"));
                });

            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}