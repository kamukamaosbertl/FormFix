package com.example.smartfit.device.util;

import android.content.Context;
import android.text.format.Formatter;

public class FormatUtils {

    public static String formatBytes(Context context, long bytes) {
        return Formatter.formatFileSize(context, bytes);
    }
}