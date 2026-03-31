package com.example.smartfit.device.collectors;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.example.smartfit.device.model.NetworkSnapshot;

public class NetworkCollector {

    public static NetworkSnapshot collect(Context context) {

        boolean connected = false;
        String networkType = "None";
        String ssid = "--";
        int linkSpeed = -1;
        String ipAddress = "--";

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            Network activeNetwork = cm.getActiveNetwork();

            if (activeNetwork != null) {
                NetworkCapabilities capabilities =
                        cm.getNetworkCapabilities(activeNetwork);

                if (capabilities != null) {
                    connected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        networkType = "WiFi";

                        WifiManager wifiManager =
                                (WifiManager) context.getApplicationContext()
                                        .getSystemService(Context.WIFI_SERVICE);

                        if (wifiManager != null) {
                            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                            if (wifiInfo != null) {
                                ssid = wifiInfo.getSSID();
                                linkSpeed = wifiInfo.getLinkSpeed();
                            }
                        }

                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        networkType = "Mobile Data";
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        networkType = "Ethernet";
                    } else {
                        networkType = "Other";
                    }
                }

                LinkProperties linkProperties = cm.getLinkProperties(activeNetwork);
                if (linkProperties != null) {
                    for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
                        if (linkAddress.getAddress() != null &&
                                !linkAddress.getAddress().isLoopbackAddress()) {
                            ipAddress = linkAddress.getAddress().getHostAddress();
                            break;
                        }
                    }
                }
            }
        }

        return new NetworkSnapshot(
                connected,
                networkType,
                ssid,
                linkSpeed,
                ipAddress
        );
    }
}