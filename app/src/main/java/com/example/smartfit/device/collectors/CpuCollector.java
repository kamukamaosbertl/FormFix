package com.example.smartfit.device.collectors;

import com.example.smartfit.device.model.CpuSnapshot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CpuCollector {

    private long lastIdle = -1;
    private long lastTotal = -1;

    public CpuSnapshot collect() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"));
            String load = reader.readLine();
            reader.close();

            if (load == null) return new CpuSnapshot(0);

            String[] toks = load.split("\\s+");

            if (toks.length < 5) return new CpuSnapshot(0);

            long user = Long.parseLong(toks[1]);
            long nice = Long.parseLong(toks[2]);
            long system = Long.parseLong(toks[3]);
            long idle = Long.parseLong(toks[4]);

            long total = user + nice + system + idle;

            // First call -> store values only
            if (lastTotal == -1) {
                lastIdle = idle;
                lastTotal = total;
                return new CpuSnapshot(0);
            }

            long diffIdle = idle - lastIdle;
            long diffTotal = total - lastTotal;

            float cpuUsage = 0;

            if (diffTotal > 0) {
                cpuUsage = (diffTotal - diffIdle) * 100f / diffTotal;
            }

            lastIdle = idle;
            lastTotal = total;

            return new CpuSnapshot(cpuUsage);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new CpuSnapshot(0);
    }
}