package com.example.smartfit.workout.model;

import com.example.smartfit.workout.model.enums.FormBreakAction;
import com.example.smartfit.workout.model.enums.VoiceFeedbackMode;

public class WorkoutPreferences {

    private VoiceFeedbackMode voiceFeedbackMode;
    private long plankTargetDurationMillis;
    private boolean plankCountdownRequiresCorrectPose;
    private boolean plankVoiceCountdownEnabled;
    private FormBreakAction plankFormBreakAction;

    public WorkoutPreferences() {
        this.voiceFeedbackMode = VoiceFeedbackMode.IMPORTANT_ONLY;
        this.plankTargetDurationMillis = 30000L;
        this.plankCountdownRequiresCorrectPose = true;
        this.plankVoiceCountdownEnabled = true;
        this.plankFormBreakAction = FormBreakAction.PAUSE_SESSION;
    }

    public VoiceFeedbackMode getVoiceFeedbackMode() {
        return voiceFeedbackMode;
    }

    public void setVoiceFeedbackMode(VoiceFeedbackMode voiceFeedbackMode) {
        if (voiceFeedbackMode != null) {
            this.voiceFeedbackMode = voiceFeedbackMode;
        }
    }

    public long getPlankTargetDurationMillis() {
        return plankTargetDurationMillis;
    }

    public void setPlankTargetDurationMillis(long plankTargetDurationMillis) {
        if (plankTargetDurationMillis > 0L) {
            this.plankTargetDurationMillis = plankTargetDurationMillis;
        }
    }

    public boolean isPlankCountdownRequiresCorrectPose() {
        return plankCountdownRequiresCorrectPose;
    }

    public void setPlankCountdownRequiresCorrectPose(boolean plankCountdownRequiresCorrectPose) {
        this.plankCountdownRequiresCorrectPose = plankCountdownRequiresCorrectPose;
    }

    public boolean isPlankVoiceCountdownEnabled() {
        return plankVoiceCountdownEnabled;
    }

    public void setPlankVoiceCountdownEnabled(boolean plankVoiceCountdownEnabled) {
        this.plankVoiceCountdownEnabled = plankVoiceCountdownEnabled;
    }

    public FormBreakAction getPlankFormBreakAction() {
        return plankFormBreakAction;
    }

    public void setPlankFormBreakAction(FormBreakAction plankFormBreakAction) {
        if (plankFormBreakAction != null) {
            this.plankFormBreakAction = plankFormBreakAction;
        }
    }

    public boolean isVoiceEnabled() {
        return voiceFeedbackMode != null && voiceFeedbackMode.isEnabled();
    }
}