package com.example.smartfit.device.model;

public class NetworkSnapshot {

    public final boolean connected;
    public final String networkType;
    public final String ssid;
    public final int linkSpeed;
    public final String ipAddress;

    public NetworkSnapshot(
            boolean connected,
            String networkType,
            String ssid,
            int linkSpeed,
            String ipAddress
    ) {
        this.connected = connected;
        this.networkType = networkType;
        this.ssid = ssid;
        this.linkSpeed = linkSpeed;
        this.ipAddress = ipAddress;
    }
}