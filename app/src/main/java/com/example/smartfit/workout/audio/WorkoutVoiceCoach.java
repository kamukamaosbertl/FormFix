package com.example.smartfit.workout.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.smartfit.workout.model.WorkoutFeedback;
import com.example.smartfit.workout.model.enums.FeedbackLevel;
import com.example.smartfit.workout.model.enums.VoiceFeedbackMode;

import java.util.Locale;

public class WorkoutVoiceCoach {

    // =========================================================
    // SPEECH TIMING RULES
    // =========================================================
    private static final long MIN_GENERAL_SPEAK_INTERVAL_MILLIS = 1800L;
    private static final long MIN_REPEAT_SAME_MESSAGE_INTERVAL_MILLIS = 4000L;

    // =========================================================
    // CORE AUDIO / TTS
    // =========================================================
    @Nullable
    private TextToSpeech textToSpeech;

    @Nullable
    private final AudioManager audioManager;

    @Nullable
    private AudioFocusRequest audioFocusRequest;

    private boolean initialized = false;

    // =========================================================
    // USER PREFERENCES
    // =========================================================
    @NonNull
    private VoiceFeedbackMode voiceFeedbackMode = VoiceFeedbackMode.IMPORTANT_ONLY;

    // =========================================================
    // SPEECH HISTORY
    // =========================================================
    @Nullable
    private String lastSpokenMessage = null;

    private long lastSpokenAtMillis = 0L;
    private int lastCountdownSecondSpoken = -1;

    public WorkoutVoiceCoach(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);

        textToSpeech = new TextToSpeech(appContext, status -> {
            if (status == TextToSpeech.SUCCESS && textToSpeech != null) {
                int result = textToSpeech.setLanguage(Locale.US);

                initialized = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED;

                if (initialized) {
                    textToSpeech.setSpeechRate(0.95f);
                    textToSpeech.setPitch(1.0f);
                    configureTtsAudioBehavior();
                    attachUtteranceListener();
                }
            }
        });
    }

    // =========================================================
    // PUBLIC SETTINGS
    // =========================================================
    public void setVoiceFeedbackMode(@NonNull VoiceFeedbackMode voiceFeedbackMode) {
        this.voiceFeedbackMode = voiceFeedbackMode;
    }

    @NonNull
    public VoiceFeedbackMode getVoiceFeedbackMode() {
        return voiceFeedbackMode;
    }

    public boolean isReady() {
        return initialized;
    }

    public boolean isEnabled() {
        return initialized && voiceFeedbackMode.isEnabled();
    }

    // =========================================================
    // PUBLIC SPEAKING METHODS
    // =========================================================
    public void speakFeedback(@Nullable WorkoutFeedback feedback) {
        if (feedback == null) {
            return;
        }

        if (!shouldSpeakFeedback(feedback)) {
            return;
        }

        speakInternal(feedback.getMessage(), false);
    }

    public void speakMessage(@Nullable String message) {
        if (!isEnabled()) {
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            return;
        }

        if (!canSpeakMessageNow(message, false)) {
            return;
        }

        speakInternal(message, false);
    }

    public void speakPriorityMessage(@Nullable String message) {
        if (!isEnabled()) {
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            return;
        }

        speakInternal(message, true);
    }

    public void speakPlankCountdown(int secondsRemaining) {
        if (!isEnabled()) {
            return;
        }

        if (secondsRemaining < 1 || secondsRemaining > 10) {
            return;
        }

        if (secondsRemaining == lastCountdownSecondSpoken) {
            return;
        }

        lastCountdownSecondSpoken = secondsRemaining;
        speakInternal(String.valueOf(secondsRemaining), true);
    }

    // =========================================================
    // RESET / STOP
    // =========================================================
    public void resetCountdownSpeech() {
        lastCountdownSecondSpoken = -1;
    }

    public void resetSpeechHistory() {
        lastSpokenMessage = null;
        lastSpokenAtMillis = 0L;
        lastCountdownSecondSpoken = -1;
    }

    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        abandonDuckAudioFocus();
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        abandonDuckAudioFocus();
        initialized = false;
    }

    // =========================================================
    // INTERNAL SPEECH RULES
    // =========================================================
    private boolean shouldSpeakFeedback(@NonNull WorkoutFeedback feedback) {
        if (!isEnabled()) {
            return false;
        }

        String message = feedback.getMessage();
        if (message.trim().isEmpty()) {
            return false;
        }

        if (voiceFeedbackMode == VoiceFeedbackMode.ALL) {
            return canSpeakMessageNow(message, false);
        }

        FeedbackLevel level = feedback.getLevel();

        if (level == FeedbackLevel.GOOD && !voiceFeedbackMode.includesPositiveFeedback()) {
            return false;
        }

        if (!feedback.shouldSpeakByDefault() && !voiceFeedbackMode.includesPositiveFeedback()) {
            return false;
        }

        return canSpeakMessageNow(message, false);
    }

    private boolean canSpeakMessageNow(@NonNull String message, boolean priority) {
        long now = System.currentTimeMillis();

        if (priority) {
            return true;
        }

        boolean isSameAsLastMessage = message.equalsIgnoreCase(
                lastSpokenMessage == null ? "" : lastSpokenMessage
        );

        if (isSameAsLastMessage
                && (now - lastSpokenAtMillis) < MIN_REPEAT_SAME_MESSAGE_INTERVAL_MILLIS) {
            return false;
        }

        return (now - lastSpokenAtMillis) >= MIN_GENERAL_SPEAK_INTERVAL_MILLIS;
    }

    private void speakInternal(@NonNull String message, boolean priority) {
        if (!initialized || textToSpeech == null) {
            return;
        }

        String cleanMessage = message.trim();
        if (cleanMessage.isEmpty()) {
            return;
        }

        requestDuckAudioFocus();

        lastSpokenMessage = cleanMessage;
        lastSpokenAtMillis = System.currentTimeMillis();

        int queueMode = priority ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
        String utteranceId = "smartfit_voice_" + System.currentTimeMillis();

        textToSpeech.speak(
                cleanMessage,
                queueMode,
                null,
                utteranceId
        );
    }

    // =========================================================
    // AUDIO FOCUS / DUCKING
    // =========================================================
    private void requestDuckAudioFocus() {
        if (audioManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes focusAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(focusAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener(focusChange -> {
                        // No special action needed for now
                    })
                    .build();

            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(
                    focusChange -> {
                        // No special action needed for now
                    },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            );
        }
    }

    private void abandonDuckAudioFocus() {
        if (audioManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            }
        } else {
            audioManager.abandonAudioFocus(null);
        }
    }

    // =========================================================
    // TTS AUDIO CONFIG
    // =========================================================
    private void configureTtsAudioBehavior() {
        if (textToSpeech == null) {
            return;
        }

        AudioAttributes ttsAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        textToSpeech.setAudioAttributes(ttsAttributes);
    }

    private void attachUtteranceListener() {
        if (textToSpeech == null) {
            return;
        }

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Ducking is already requested before speaking
            }

            @Override
            public void onDone(String utteranceId) {
                abandonDuckAudioFocus();
            }

            @Override
            public void onError(String utteranceId) {
                abandonDuckAudioFocus();
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                abandonDuckAudioFocus();
            }
        });
    }
}