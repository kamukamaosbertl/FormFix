package com.example.smartfit.device.monitor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.ConnectivityManager.NetworkCallback;

import androidx.annotation.NonNull;

import com.example.smartfit.device.collectors.NetworkCollector;
import com.example.smartfit.device.model.NetworkSnapshot;

public class ConnectivityLiveMonitor {

    public interface Listener {
        void onNetworkChanged(NetworkSnapshot snapshot);
    }

    private final Context context;
    private final Listener listener;
    private ConnectivityManager connectivityManager;
    private NetworkCallback networkCallback;

    public ConnectivityLiveMonitor(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public void start() {
        connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) return;

        networkCallback = new NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                listener.onNetworkChanged(NetworkCollector.collect(context));
            }

            @Override
            public void onLost(@NonNull Network network) {
                listener.onNetworkChanged(NetworkCollector.collect(context));
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network,
                                              @NonNull android.net.NetworkCapabilities networkCapabilities) {
                listener.onNetworkChanged(NetworkCollector.collect(context));
            }
        };

        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    public void stop() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception ignored) {
            }
        }
    }
}