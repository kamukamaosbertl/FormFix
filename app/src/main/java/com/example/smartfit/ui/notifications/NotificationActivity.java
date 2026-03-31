package com.example.smartfit.ui.notifications;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.TimePickerDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartfit.R;
import com.example.smartfit.notifications.NotificationHelper;
import com.example.smartfit.notifications.NotificationPayload;
import com.example.smartfit.notifications.ReminderScheduler;
import com.example.smartfit.notifications.RemoteNotificationRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private TextView tvNotificationStatus;
    private TextView tvFcmToken;
    private TextView tvSelectedTime;
    private ImageView ivPermissionIndicator;

    private TextInputEditText etLocalTitle;
    private TextInputEditText etLocalMessage;
    private TextInputEditText etRemoteTitle;
    private TextInputEditText etRemoteMessage;
    private TextInputEditText etRemoteTarget;

    private TextInputLayout tilRemoteTarget;

    private Chip chipTargetTopic;
    private Chip chipTargetToken;

    private MaterialButton btnRequestPermission;
    private MaterialButton btnSendLocal;
    private MaterialButton btnScheduleReminder;
    private MaterialButton btnCancelReminder;
    private MaterialButton btnGetFcmToken;
    private MaterialButton btnCopyFcmToken;
    private MaterialButton btnSendRemote;
    private MaterialButton btnPickTime;

    private int selectedHour = 7;
    private int selectedMinute = 0;

    private RemoteNotificationRepository remoteNotificationRepository;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    updatePermissionViews(true);
                    showSnack("Notifications enabled");
                } else {
                    updatePermissionViews(false);
                    showSnack("Notification permission denied");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        bindViews();

        remoteNotificationRepository = new RemoteNotificationRepository();

        NotificationHelper.createNotificationChannel(this);
        updateSelectedTimeLabel();
        updatePermissionStatus();
        updateRemoteTargetUi();

        btnRequestPermission.setOnClickListener(v -> requestNotificationPermission());
        btnSendLocal.setOnClickListener(v -> sendCustomLocalNotification());

        btnPickTime.setOnClickListener(v -> openTimePicker());

        btnScheduleReminder.setOnClickListener(v -> {
            String title = getText(etLocalTitle, "SmartFit Reminder");
            String message = getText(etLocalMessage, "Time for your workout. Open SmartFit and train smart today.");

            ReminderScheduler.scheduleDailyReminder(
                    this,
                    title,
                    message,
                    selectedHour,
                    selectedMinute
            );

            showSnack("Daily reminder scheduled for " + formatTime(selectedHour, selectedMinute));
        });

        btnCancelReminder.setOnClickListener(v -> {
            ReminderScheduler.cancelDailyReminder(this);
            showSnack("Daily reminder cancelled");
        });

        btnGetFcmToken.setOnClickListener(v -> fetchFcmToken());
        btnCopyFcmToken.setOnClickListener(v -> copyFcmToken());
        btnSendRemote.setOnClickListener(v -> sendRemoteNotificationRequest());

        chipTargetTopic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                updateRemoteTargetUi();
            }
        });

        chipTargetToken.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                updateRemoteTargetUi();
            }
        });

        FirebaseMessaging.getInstance()
                .subscribeToTopic("smartfit_general")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showSnack("Subscribed to smartfit_general");
                    }
                });
    }

    private void bindViews() {
        tvNotificationStatus = findViewById(R.id.tv_notification_status);
        tvFcmToken = findViewById(R.id.tv_fcm_token);
        tvSelectedTime = findViewById(R.id.tv_selected_time);
        ivPermissionIndicator = findViewById(R.id.iv_permission_indicator);

        etLocalTitle = findViewById(R.id.et_local_title);
        etLocalMessage = findViewById(R.id.et_local_message);
        etRemoteTitle = findViewById(R.id.et_remote_title);
        etRemoteMessage = findViewById(R.id.et_remote_message);
        etRemoteTarget = findViewById(R.id.et_remote_target);

        tilRemoteTarget = findViewById(R.id.til_remote_target);

        chipTargetTopic = findViewById(R.id.chip_target_topic);
        chipTargetToken = findViewById(R.id.chip_target_token);

        btnRequestPermission = findViewById(R.id.btn_request_permission);
        btnSendLocal = findViewById(R.id.btn_send_local);
        btnScheduleReminder = findViewById(R.id.btn_schedule_reminder);
        btnCancelReminder = findViewById(R.id.btn_cancel_reminder);
        btnGetFcmToken = findViewById(R.id.btn_get_fcm_token);
        btnCopyFcmToken = findViewById(R.id.btn_copy_fcm_token);
        btnSendRemote = findViewById(R.id.btn_send_remote);
        btnPickTime = findViewById(R.id.btn_pick_time);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {
                updatePermissionViews(true);
                showSnack("Notifications already enabled");
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            tvNotificationStatus.setText("Notifications ready on this Android version");
            showSnack("Permission not required on this Android version");
        }
    }

    private void updatePermissionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;

            updatePermissionViews(granted);
        } else {
            tvNotificationStatus.setText("Notifications ready on this Android version");
            ivPermissionIndicator.setAlpha(1f);
        }
    }

    private void updatePermissionViews(boolean granted) {
        tvNotificationStatus.setText(
                granted ? "Notifications enabled" : "Notifications need permission"
        );
        ivPermissionIndicator.setAlpha(granted ? 1f : 0.45f);
    }

    private void sendCustomLocalNotification() {
        String title = getText(etLocalTitle, "SmartFit Reminder");
        String message = getText(etLocalMessage, "Time for your workout. Open SmartFit and train smart today.");

        ReminderScheduler.triggerTestNotification(this, title, message);
        showSnack("Local notification sent");
    }

    private void openTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateSelectedTimeLabel();
                },
                selectedHour,
                selectedMinute,
                true
        );

        dialog.show();
    }

    private void updateSelectedTimeLabel() {
        tvSelectedTime.setText("Reminder time: " + formatTime(selectedHour, selectedMinute));
    }

    private String formatTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    private void updateRemoteTargetUi() {
        boolean isTopic = chipTargetTopic.isChecked();

        if (isTopic) {
            tilRemoteTarget.setHint("Target topic");
            etRemoteTarget.setText("smartfit_general");
        } else {
            tilRemoteTarget.setHint("Target device token");
            etRemoteTarget.setText("");
        }
    }

    private void sendRemoteNotificationRequest() {
        String title = getText(etRemoteTitle, "");
        String message = getText(etRemoteMessage, "");
        String target = getText(etRemoteTarget, "");

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(message) || TextUtils.isEmpty(target)) {
            showSnack("Please fill title, message, and target");
            return;
        }

        String targetType = chipTargetTopic.isChecked() ? "topic" : "token";

        NotificationPayload payload = new NotificationPayload(
                title,
                message,
                targetType,
                target
        );

        btnSendRemote.setEnabled(false);

        remoteNotificationRepository.sendNotification(payload, new RemoteNotificationRepository.Callback() {
            @Override
            public void onSuccess() {
                btnSendRemote.setEnabled(true);
                showSnack("Remote notification request sent to backend layer");
            }

            @Override
            public void onError(String message) {
                btnSendRemote.setEnabled(true);
                showSnack(message);
            }
        });
    }

    private void fetchFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        tvFcmToken.setText("Failed to get FCM token");
                        showSnack("Failed to fetch token");
                        return;
                    }

                    String token = task.getResult();
                    tvFcmToken.setText(token);
                    showSnack("Device token fetched");
                });
    }

    private void copyFcmToken() {
        String token = tvFcmToken.getText() != null
                ? tvFcmToken.getText().toString().trim()
                : "";

        if (token.isEmpty() || token.equalsIgnoreCase("FCM token will appear here")) {
            showSnack("No token to copy yet");
            return;
        }

        ClipboardManager clipboardManager =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboardManager != null) {
            ClipData clipData = ClipData.newPlainText("FCM Token", token);
            clipboardManager.setPrimaryClip(clipData);
            showSnack("Token copied");
        } else {
            showSnack("Clipboard unavailable");
        }
    }

    private String getText(TextInputEditText editText, String fallback) {
        if (editText == null || editText.getText() == null) {
            return fallback;
        }

        String value = editText.getText().toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}