package com.example.smartfit.device.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkStateReceiver extends BroadcastReceiver {

    public interface Listener {
        void onSystemEvent(String eventMessage);
    }

    private final Listener listener;

    public NetworkStateReceiver(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        String message;

        switch (action) {
            case Intent.ACTION_AIRPLANE_MODE_CHANGED:
                boolean isOn = intent.getBooleanExtra("state", false);
                message = isOn
                        ? "✈️ Airplane mode enabled"
                        : "📶 Airplane mode disabled";
                break;

            case Intent.ACTION_POWER_CONNECTED:
                message = "🔌 Charger connected";
                break;

            case Intent.ACTION_POWER_DISCONNECTED:
                message = "🔋 Charger disconnected";
                break;

            default:
                message = "📢 System event detected";
                break;
        }

        if (listener != null) {
            listener.onSystemEvent(message);
        }
    }
}