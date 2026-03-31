package com.example.smartfit.device.model;

public class StorageSnapshot {

    private final long totalBytes;
    private final long usedBytes;
    private final long freeBytes;

    public StorageSnapshot(long totalBytes, long usedBytes, long freeBytes) {
        this.totalBytes = totalBytes;
        this.usedBytes = usedBytes;
        this.freeBytes = freeBytes;
    }

    public long getTotal() {
        return totalBytes;
    }

    public long getUsed() {
        return usedBytes;
    }

    public long getAvailable() {
        return freeBytes;
    }

    public int getUsedPercent() {
        if (totalBytes == 0) return 0;
        return (int) ((usedBytes * 100) / totalBytes);
    }


    public int getFreePercent() {
        if (totalBytes == 0) return 0;
        return (int) ((freeBytes * 100) / totalBytes);
    }
}