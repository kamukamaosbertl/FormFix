package com.example.smartfit.device.collectors;

import android.os.Environment;
import android.os.StatFs;

import com.example.smartfit.device.model.StorageSnapshot;

import java.io.File;

public class StorageCollector {

    public static StorageSnapshot collect() {

        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();

        long total = totalBlocks * blockSize;
        long free = availableBlocks * blockSize;
        long used = total - free;

        return new StorageSnapshot(total, used, free);
    }
}