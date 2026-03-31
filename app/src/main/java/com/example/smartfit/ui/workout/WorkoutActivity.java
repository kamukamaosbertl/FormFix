package com.example.smartfit.ui.workout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.annotation.NonNull;

import com.example.smartfit.R;
import com.example.smartfit.workout.engine.PoseAnalyzer;
import com.example.smartfit.workout.engine.PoseFrameAnalyzer;
import com.example.smartfit.workout.engine.PoseOverlayMapper;
import com.example.smartfit.workout.engine.WorkoutFeedbackEngine;
import com.example.smartfit.workout.manager.WorkoutSessionManager;
import com.example.smartfit.workout.model.PoseFrameData;
import com.example.smartfit.workout.model.WorkoutFeedback;
import com.example.smartfit.workout.model.WorkoutSession;
import com.example.smartfit.workout.model.WorkoutPreferences;
import com.example.smartfit.workout.model.enums.WorkoutType;
import com.example.smartfit.workout.model.enums.VoiceFeedbackMode;
import com.example.smartfit.workout.model.enums.FormBreakAction;
import com.example.smartfit.workout.repository.WorkoutRepository;
import com.example.smartfit.workout.audio.WorkoutVoiceCoach;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.pose.Pose;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutActivity extends AppCompatActivity {

    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;

    // =========================================================
    // VIEW REFERENCES
    // =========================================================
    private PreviewView previewView;
    private PoseOverlayView poseOverlayView;

    private TextView tvWorkoutTitle;
    private TextView tvSessionState;
    private TextView tvTimer;
    private TextView tvMetricLabel;
    private TextView tvMetricValue;
    private TextView tvFeedback;

    private TextView tvCountdownValue;
    private TextView tvCountdownLabel;

    private Chip btnSelectPushup, btnSelectSquat, btnSelectSitup, btnSelectPlank;

    private MaterialButton btnMainAction;
    private MaterialButton btnEndSession;
    private AppCompatImageButton btnPreferences;

    private MaterialCardView cardControls;
    private MaterialCardView cardCountdown;

    // =========================================================
    // CORE DEPENDENCIES
    // =========================================================
    private WorkoutSessionManager sessionManager;
    private final PoseOverlayMapper overlayMapper = new PoseOverlayMapper();

    // =========================================================
    // STATE
    // =========================================================
    private WorkoutType selectedWorkoutType = WorkoutType.PUSH_UP;
    private boolean isPaused = false;
    private boolean timerRunning = false;

    // =========================================================
    // CAMERA
    // =========================================================
    private ExecutorService cameraExecutor;
    private PoseFrameAnalyzer poseFrameAnalyzer;

    // =========================================================
    // PREFERENCES / AUDIO
    // =========================================================
    private WorkoutPreferences workoutPreferences;
    private WorkoutVoiceCoach voiceCoach;

    // =========================================================
    // TIMER
    // =========================================================
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimerUI();

            if (timerRunning) {
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    // =========================================================
    // PERMISSION HANDLER
    // =========================================================
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else tvFeedback.setText("Camera permission is required.");
            });

    // =========================================================
    // LIFECYCLE
    // =========================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        bindViews();
        setupDependencies();
        setupSessionManager();
        setupListeners();

        renderInitialState();
        checkCameraPermissionAndStart();
    }

    // =========================================================
    // VIEW BINDING
    // =========================================================
    private void bindViews() {
        previewView = findViewById(R.id.preview_view);
        poseOverlayView = findViewById(R.id.pose_overlay);

        tvWorkoutTitle = findViewById(R.id.tv_workout_title);
        tvSessionState = findViewById(R.id.tv_session_state);
        tvTimer = findViewById(R.id.tv_timer);
        tvMetricLabel = findViewById(R.id.tv_primary_metric_label);
        tvMetricValue = findViewById(R.id.tv_primary_metric_value);
        tvFeedback = findViewById(R.id.tv_feedback);

        tvCountdownValue = findViewById(R.id.tv_countdown_value);
        tvCountdownLabel = findViewById(R.id.tv_countdown_label);

        btnSelectPushup = findViewById(R.id.btn_select_pushup);
        btnSelectSquat = findViewById(R.id.btn_select_squat);
        btnSelectSitup = findViewById(R.id.btn_select_situp);
        btnSelectPlank = findViewById(R.id.btn_select_plank);

        btnMainAction = findViewById(R.id.btn_main_action);
        btnEndSession = findViewById(R.id.btn_end_session);
        btnPreferences = findViewById(R.id.btn_preferences);

        cardControls = findViewById(R.id.card_controls);
        cardCountdown = findViewById(R.id.card_countdown_overlay);
    }

    // =========================================================
    // SETUP
    // =========================================================
    private void setupDependencies() {
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Create default workout preferences for this screen
        workoutPreferences = new WorkoutPreferences();

        // Create voice coach and immediately apply current preference mode
        voiceCoach = new WorkoutVoiceCoach(this);
        voiceCoach.setVoiceFeedbackMode(workoutPreferences.getVoiceFeedbackMode());
    }

    private void setupSessionManager() {
        sessionManager = new WorkoutSessionManager(
                WorkoutRepository.getInstance(),
                new WorkoutFeedbackEngine(),
                new PoseAnalyzer(),
                new WorkoutSessionManager.Listener() {

                    @Override
                    public void onRepUpdated(int repCount) {
                        runOnUiThread(() -> tvMetricValue.setText(String.valueOf(repCount)));
                    }

                    @Override
                    public void onDurationUpdated(long remaining, long hold) {
                        runOnUiThread(() -> {
                            tvMetricValue.setText(formatDuration(remaining));

                            if (remaining <= 10000) {
                                long secondsLong = Math.max(1, (remaining + 999) / 1000);
                                int secondsRemaining = (int) secondsLong;

                                showCountdownOverlay(String.valueOf(secondsRemaining), "Seconds");

                                if (workoutPreferences.isPlankVoiceCountdownEnabled()) {
                                    voiceCoach.speakPlankCountdown(secondsRemaining);
                                }
                            } else {
                                hideCountdownOverlay();
                            }
                        });
                    }

                    @Override
                    public void onFeedbackUpdated(WorkoutFeedback feedback) {
                        runOnUiThread(() -> {
                            tvFeedback.setText(feedback.getMessage());
                            voiceCoach.speakFeedback(feedback);
                        });
                    }

                    @Override
                    public void onSessionCompleted(WorkoutSession session) {
                        runOnUiThread(() -> {
                            voiceCoach.resetSpeechHistory();
                            renderSessionCompleted(session);
                        });
                    }

                    @Override
                    public void onSessionCancelled(WorkoutSession session) {
                        runOnUiThread(() -> {
                            voiceCoach.resetSpeechHistory();
                            renderSessionCancelled(session);
                        });
                    }

                    @Override
                    public void onSessionPaused(WorkoutSession session) {
                        runOnUiThread(() -> renderPaused());
                    }

                    @Override
                    public void onSessionResumed(WorkoutSession session) {
                        runOnUiThread(() -> renderResumed());
                    }
                }
        );

        // IMPORTANT:
        // Give the manager the same preferences object used by this activity.
        // Any updates made later from the preferences menu will affect session behavior.
        sessionManager.setPreferences(workoutPreferences);
    }

    private void setupListeners() {
        setupWorkoutSelectors();
        setupButtons();
    }

    // =========================================================
    // UI STATES
    // =========================================================
    private void renderInitialState() {
        tvWorkoutTitle.setText(formatWorkoutType(selectedWorkoutType));
        tvSessionState.setText("Idle");
        tvTimer.setText("00:00");
        tvMetricLabel.setText("Reps");
        tvMetricValue.setText("0");
        tvFeedback.setText("Choose a workout and get into frame.");

        btnMainAction.setText("Start Session");

        showControlsPanel();
    }

    private void renderSessionStarted() {
        tvSessionState.setText("Running");
        btnMainAction.setText("Pause");

        hideControlsPanel();
        startTimer();
    }

    private void renderPaused() {
        tvSessionState.setText("Paused");
        btnMainAction.setText("Resume");

        showControlsPanel();
        stopTimer();
    }

    private void renderResumed() {
        tvSessionState.setText("Running");
        btnMainAction.setText("Pause");

        hideControlsPanel();
        startTimer();
    }

    private void renderSessionCompleted(WorkoutSession session) {
        tvSessionState.setText("Completed");
        tvFeedback.setText("Great job!");

        stopTimer();
        showControlsPanel();
        btnMainAction.setText("Start Session");
    }

    private void renderSessionCancelled(WorkoutSession session) {
        tvSessionState.setText("Cancelled");
        tvFeedback.setText("Session cancelled");

        stopTimer();
        showControlsPanel();
    }

    // =========================================================
    // WORKOUT SELECTION
    // =========================================================
    private void setupWorkoutSelectors() {
        btnSelectPushup.setOnClickListener(v -> selectWorkout(WorkoutType.PUSH_UP));
        btnSelectSquat.setOnClickListener(v -> selectWorkout(WorkoutType.SQUAT));
        btnSelectSitup.setOnClickListener(v -> selectWorkout(WorkoutType.SIT_UP));
        btnSelectPlank.setOnClickListener(v -> selectWorkout(WorkoutType.PLANK));
    }

    private void selectWorkout(WorkoutType type) {
        if (sessionManager.getCurrentSession() != null) return;

        selectedWorkoutType = type;

        tvWorkoutTitle.setText(formatWorkoutType(type));

        // Change metric label depending on workout
        if (type == WorkoutType.PLANK) {
            tvMetricLabel.setText("Remaining");
        } else {
            tvMetricLabel.setText("Reps");
        }

        tvFeedback.setText("Selected " + formatWorkoutType(type));
    }

    // =========================================================
    // BUTTON ACTIONS
    // =========================================================
    private void setupButtons() {
        btnMainAction.setOnClickListener(v -> handleMainAction());
        btnEndSession.setOnClickListener(v -> sessionManager.endSession());
        btnPreferences.setOnClickListener(v -> openWorkoutPreferences());
    }

    private void handleMainAction() {
        WorkoutSession session = sessionManager.getCurrentSession();

        if (session == null) {
            sessionManager.startSession(selectedWorkoutType);
            renderSessionStarted();
            return;
        }

        if (isPaused) {
            sessionManager.resumeSession();
            isPaused = false;
        } else {
            sessionManager.pauseSession();
            isPaused = true;
        }
    }

    // =========================================================
    // PREFERENCES BOTTOM SHEETS
    // =========================================================
    private void openWorkoutPreferences() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_workout_preferences, null, false);

        dialog.setContentView(sheetView);

        // -----------------------------------------------------
        // Bind sheet views
        // -----------------------------------------------------
        RadioGroup radioGroupVoiceMode = sheetView.findViewById(R.id.radio_group_voice_mode);
        RadioButton radioVoiceOff = sheetView.findViewById(R.id.radio_voice_off);
        RadioButton radioVoiceImportant = sheetView.findViewById(R.id.radio_voice_important);
        RadioButton radioVoiceAll = sheetView.findViewById(R.id.radio_voice_all);

        Chip chipPlank15 = sheetView.findViewById(R.id.chip_plank_15);
        Chip chipPlank30 = sheetView.findViewById(R.id.chip_plank_30);
        Chip chipPlank45 = sheetView.findViewById(R.id.chip_plank_45);
        Chip chipPlank60 = sheetView.findViewById(R.id.chip_plank_60);

        MaterialSwitch switchRequireCorrectPose =
                sheetView.findViewById(R.id.switch_require_correct_pose);
        MaterialSwitch switchPlankVoiceCountdown =
                sheetView.findViewById(R.id.switch_plank_voice_countdown);

        RadioGroup radioGroupFormBreakAction =
                sheetView.findViewById(R.id.radio_group_form_break_action);
        RadioButton radioBreakPause = sheetView.findViewById(R.id.radio_break_pause);
        RadioButton radioBreakCancel = sheetView.findViewById(R.id.radio_break_cancel);

        MaterialButton btnSavePreferences =
                sheetView.findViewById(R.id.btn_save_preferences);

        // -----------------------------------------------------
        // Pre-fill current preference values into the UI
        // -----------------------------------------------------
        bindPreferencesToSheet(
                radioVoiceOff,
                radioVoiceImportant,
                radioVoiceAll,
                chipPlank15,
                chipPlank30,
                chipPlank45,
                chipPlank60,
                switchRequireCorrectPose,
                switchPlankVoiceCountdown,
                radioBreakPause,
                radioBreakCancel
        );

        // -----------------------------------------------------
        // Save button action
        // -----------------------------------------------------
        btnSavePreferences.setOnClickListener(v -> {
            applyPreferencesFromSheet(
                    radioVoiceOff,
                    radioVoiceImportant,
                    radioVoiceAll,
                    chipPlank15,
                    chipPlank30,
                    chipPlank45,
                    chipPlank60,
                    switchRequireCorrectPose,
                    switchPlankVoiceCountdown,
                    radioBreakPause,
                    radioBreakCancel
            );

            // Sync changed preferences into dependent systems
            sessionManager.setPreferences(workoutPreferences);
            voiceCoach.setVoiceFeedbackMode(workoutPreferences.getVoiceFeedbackMode());

            tvFeedback.setText("Preferences updated.");
            voiceCoach.speakPriorityMessage("Preferences updated");

            dialog.dismiss();
        });

        dialog.show();
    }

    // =========================================================
    // PREFERENCES UI -> FILL CURRENT VALUES
    // =========================================================
    private void bindPreferencesToSheet(
            @NonNull RadioButton radioVoiceOff,
            @NonNull RadioButton radioVoiceImportant,
            @NonNull RadioButton radioVoiceAll,
            @NonNull Chip chipPlank15,
            @NonNull Chip chipPlank30,
            @NonNull Chip chipPlank45,
            @NonNull Chip chipPlank60,
            @NonNull MaterialSwitch switchRequireCorrectPose,
            @NonNull MaterialSwitch switchPlankVoiceCountdown,
            @NonNull RadioButton radioBreakPause,
            @NonNull RadioButton radioBreakCancel
    ) {
        // -----------------------------------------------------
        // Voice mode
        // -----------------------------------------------------
        switch (workoutPreferences.getVoiceFeedbackMode()) {
            case OFF:
                radioVoiceOff.setChecked(true);
                break;

            case IMPORTANT_ONLY:
                radioVoiceImportant.setChecked(true);
                break;

            case ALL:
                radioVoiceAll.setChecked(true);
                break;
        }

        // -----------------------------------------------------
        // Plank target duration
        // -----------------------------------------------------
        long duration = workoutPreferences.getPlankTargetDurationMillis();

        if (duration == 15000L) {
            chipPlank15.setChecked(true);
        } else if (duration == 30000L) {
            chipPlank30.setChecked(true);
        } else if (duration == 45000L) {
            chipPlank45.setChecked(true);
        } else {
            chipPlank60.setChecked(true);
        }

        // -----------------------------------------------------
        // Switches
        // -----------------------------------------------------
        switchRequireCorrectPose.setChecked(
                workoutPreferences.isPlankCountdownRequiresCorrectPose()
        );

        switchPlankVoiceCountdown.setChecked(
                workoutPreferences.isPlankVoiceCountdownEnabled()
        );

        // -----------------------------------------------------
        // Form break action
        // -----------------------------------------------------
        if (workoutPreferences.getPlankFormBreakAction() == FormBreakAction.CANCEL_SESSION) {
            radioBreakCancel.setChecked(true);
        } else {
            radioBreakPause.setChecked(true);
        }
    }

    // =========================================================
    // PREFERENCES UI -> APPLY CHANGES TO REAL MODEL
    // =========================================================
    private void applyPreferencesFromSheet(
            @NonNull RadioButton radioVoiceOff,
            @NonNull RadioButton radioVoiceImportant,
            @NonNull RadioButton radioVoiceAll,
            @NonNull Chip chipPlank15,
            @NonNull Chip chipPlank30,
            @NonNull Chip chipPlank45,
            @NonNull Chip chipPlank60,
            @NonNull MaterialSwitch switchRequireCorrectPose,
            @NonNull MaterialSwitch switchPlankVoiceCountdown,
            @NonNull RadioButton radioBreakPause,
            @NonNull RadioButton radioBreakCancel
    ) {
        // -----------------------------------------------------
        // Voice feedback mode
        // -----------------------------------------------------
        if (radioVoiceOff.isChecked()) {
            workoutPreferences.setVoiceFeedbackMode(VoiceFeedbackMode.OFF);
        } else if (radioVoiceAll.isChecked()) {
            workoutPreferences.setVoiceFeedbackMode(VoiceFeedbackMode.ALL);
        } else {
            workoutPreferences.setVoiceFeedbackMode(VoiceFeedbackMode.IMPORTANT_ONLY);
        }

        // -----------------------------------------------------
        // Plank target duration
        // -----------------------------------------------------
        if (chipPlank15.isChecked()) {
            workoutPreferences.setPlankTargetDurationMillis(15000L);
        } else if (chipPlank30.isChecked()) {
            workoutPreferences.setPlankTargetDurationMillis(30000L);
        } else if (chipPlank45.isChecked()) {
            workoutPreferences.setPlankTargetDurationMillis(45000L);
        } else if (chipPlank60.isChecked()) {
            workoutPreferences.setPlankTargetDurationMillis(60000L);
        }

        // -----------------------------------------------------
        // Plank switches
        // -----------------------------------------------------
        workoutPreferences.setPlankCountdownRequiresCorrectPose(
                switchRequireCorrectPose.isChecked()
        );

        workoutPreferences.setPlankVoiceCountdownEnabled(
                switchPlankVoiceCountdown.isChecked()
        );

        // -----------------------------------------------------
        // Form break action
        // -----------------------------------------------------
        if (radioBreakCancel.isChecked()) {
            workoutPreferences.setPlankFormBreakAction(FormBreakAction.CANCEL_SESSION);
        } else {
            workoutPreferences.setPlankFormBreakAction(FormBreakAction.PAUSE_SESSION);
        }
    }

    // =========================================================
    // CAMERA
    // =========================================================
    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(CAMERA_PERMISSION);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                poseFrameAnalyzer = new PoseFrameAnalyzer(new PoseFrameAnalyzer.Listener() {
                    @Override
                    public void onPoseDataReady(PoseFrameData data, Pose pose, int imageWidth, int imageHeight, int rotationDegrees) {

                        List<PointF> points = overlayMapper.mapToOverlay(
                                pose, imageWidth, imageHeight, rotationDegrees ,previewView, true
                        );

                        runOnUiThread(() ->
                                poseOverlayView.setPoseData(points, overlayMapper.getConnections())
                        );

                        if (sessionManager.getCurrentSession() != null && !isPaused) {
                            sessionManager.processFrame(data);
                        }
                    }

                    @Override
                    public void onPoseMissing() {
                        runOnUiThread(() -> poseOverlayView.clearPose());
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> tvFeedback.setText(
                                e.getMessage() != null ? e.getMessage() : "Pose analysis error"
                        ));
                    }
                });

                analysis.setAnalyzer(cameraExecutor, poseFrameAnalyzer);

                provider.unbindAll();
                provider.bindToLifecycle(this,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        analysis);

            } catch (Exception e) {
                tvFeedback.setText("Camera failed");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // =========================================================
    // TIMER
    // =========================================================
    private void startTimer() {
        if (timerRunning) return;

        timerRunning = true;
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        timerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void updateTimerUI() {
        WorkoutSession session = sessionManager.getCurrentSession();
        if (session != null) {
            tvTimer.setText(formatDuration(session.getDurationMillis()));
        }
    }

    // =========================================================
    // OVERLAY CONTROL
    // =========================================================
    private void showCountdownOverlay(String value, String label) {
        cardCountdown.setVisibility(View.VISIBLE);
        tvCountdownValue.setText(value);
        tvCountdownLabel.setText(label);
    }

    private void hideCountdownOverlay() {
        cardCountdown.setVisibility(View.GONE);
    }

    private void showControlsPanel() {
        cardControls.setVisibility(View.VISIBLE);
    }

    private void hideControlsPanel() {
        cardControls.setVisibility(View.GONE);
    }

    // =========================================================
    // UTILITIES
    // =========================================================
    private String formatWorkoutType(WorkoutType type) {
        String raw = type.name().toLowerCase(Locale.getDefault()).replace("_", " ");
        return raw.substring(0, 1).toUpperCase() + raw.substring(1);
    }

    private String formatDuration(long millis) {
        long sec = millis / 1000;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    // =========================================================
    // CLEANUP
    // =========================================================
    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTimer();

        if (poseFrameAnalyzer != null) poseFrameAnalyzer.stop();
        if (poseOverlayView != null) poseOverlayView.release();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}