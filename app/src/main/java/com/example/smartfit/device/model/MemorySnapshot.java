package com.example.smartfit.device.model;

public class MemorySnapshot {

    private final long total;
    private final long available;
    private final long used;

    public MemorySnapshot(long total, long available){
        this.total = total;
        this.available = available;
        this.used = total - available;
    }

    public long getTotal() {
        return total;
    }

    public long getAvailable() {
        return available;
    }

    public long getUsed() {
        return used;
    }

    public int getUsedPercent() {
        if (total == 0) return 0;
        return (int) ((used * 100) / total);
    }

    public int getAvailablePercent() {
        if (total == 0) return 0;
        return (int) ((available * 100) / total);
    }

    // UI friendly helpers

    public long getTotalMB(){
        return total / (1024 * 1024);
    }

    public long getUsedMB(){
        return used / (1024 * 1024);
    }

    public long getAvailableMB(){
        return available / (1024 * 1024);
    }
}