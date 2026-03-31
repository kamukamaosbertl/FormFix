package com.example.smartfit.device;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfit.R;
import com.example.smartfit.device.sensor.SensorListFragment;

public class SensorMonitorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sensor_monitor);

        if (savedInstanceState == null) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            new SensorListFragment()
                    )
                    .commit();
        }
    }
}