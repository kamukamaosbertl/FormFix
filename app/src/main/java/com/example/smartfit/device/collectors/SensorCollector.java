package com.example.smartfit.device.collectors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.smartfit.device.model.SensorReading;

public class SensorCollector implements SensorEventListener {

    public interface Listener {
        void onSensorUpdate(SensorReading reading);
        void onSensorUnavailable();
    }

    private final SensorManager sensorManager;
    private final Sensor sensor;
    private final Listener listener;

    public SensorCollector(Context context, int sensorType, Listener listener) {

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager != null ? sensorManager.getDefaultSensor(sensorType) : null;
        this.listener = listener;
    }

    public void start() {
        if (sensorManager == null || listener == null) return;

        if (sensor != null) {
            sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME
            );
        } else {
            listener.onSensorUnavailable();
        }
    }

    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    public static boolean isMotionSensor(int sensorType){
        return sensorType == Sensor.TYPE_ACCELEROMETER
                || sensorType == Sensor.TYPE_GYROSCOPE
                || sensorType == Sensor.TYPE_GRAVITY
                || sensorType == Sensor.TYPE_LINEAR_ACCELERATION;
    }

    public static boolean isSingleValueSensor(int sensorType){
        return sensorType == Sensor.TYPE_PROXIMITY
                || sensorType == Sensor.TYPE_STEP_COUNTER;
    }

    public static boolean shouldShowGraph(int sensorType){
        return isMotionSensor(sensorType);
    }

    public static String getSensorCategoryLabel(int sensorType){
        if (isMotionSensor(sensorType)){
            return "Motion Sensor";
        }

        if (isSingleValueSensor(sensorType)){
            return "Single Value Sensor";
        }

        return "General Sensor";
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values.length > 0 ? event.values[0] : 0f;
        float y = event.values.length > 1 ? event.values[1] : 0f;
        float z = event.values.length > 2 ? event.values[2] : 0f;

        SensorReading reading = new SensorReading(
                event.sensor.getType(),
                x,
                y,
                z,
                event.values.length
        );

        listener.onSensorUpdate(reading);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}