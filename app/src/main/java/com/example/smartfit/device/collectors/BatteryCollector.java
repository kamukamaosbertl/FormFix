package com.example.smartfit.device.collectors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.example.smartfit.device.model.BatterySnapshot;

public class BatteryCollector {

    public static BatterySnapshot collect(Context context){

        int percent = -1;
        boolean charging = false;
        int temp = -1;
        long voltage = -1;

        // Battery percentage (no permission)
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (bm != null){
            int p = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            if (p >= 0 && p <= 100) percent = p;
        }

        // changing state via sticky broadcast
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent != null) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            charging = (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                    (status == BatteryManager.BATTERY_STATUS_FULL);

            // Temperature (tenths of °C)
            int rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            if (rawTemp != -1) {
                temp = rawTemp / 10;   // convert to °C
            }

            // Voltage (millivolts)
            voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        }


        return new BatterySnapshot(percent, charging, temp, voltage);
    }
}
