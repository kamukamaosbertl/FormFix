package com.example.smartfit.device.sensor;

import android.hardware.Sensor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfit.R;
import com.example.smartfit.device.adapter.SensorAdapter;
import com.example.smartfit.device.model.SensorSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SensorListFragment extends Fragment {

    private RecyclerView recyclerSensors;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_sensor_list,
                container,
                false
        );

        recyclerSensors = view.findViewById(R.id.recycler_sensors);
        setupSensors();

        return view;
    }

    private void setupSensors() {
        List<SensorSnapshot> sensors = new ArrayList<>();

        sensors.add(new SensorSnapshot(
                Sensor.TYPE_ACCELEROMETER,
                "Accelerometer",
                "Tracks movement and motion on X, Y, Z axes",
                "📈"
        ));

        sensors.add(new SensorSnapshot(
                Sensor.TYPE_GYROSCOPE,
                "Gyroscope",
                "Measures device rotation and angular movement",
                "🌀"
        ));

        sensors.add(new SensorSnapshot(
                Sensor.TYPE_GRAVITY,
                "Gravity Sensor",
                "Shows gravity direction for tilt and posture",
                "🌍"
        ));

        sensors.add(new SensorSnapshot(
                Sensor.TYPE_LINEAR_ACCELERATION,
                "Linear Acceleration",
                "Measures movement without gravity effect",
                "🏃"
        ));

        sensors.add(new SensorSnapshot(
                Sensor.TYPE_PROXIMITY,
                "Proximity",
                "Detects nearby objects or hand closeness",
                "🤏"
        ));

        sensors.add(new SensorSnapshot(
                Sensor.TYPE_STEP_COUNTER,
                "Step Counter",
                "Counts total steps for fitness tracking",
                "👣"
        ));

        SensorAdapter adapter = new SensorAdapter(
                sensors,
                this::openSensorDetail
        );

        recyclerSensors.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerSensors.setAdapter(adapter);
    }

    private void openSensorDetail(SensorSnapshot sensor) {
        SensorDetailFragment fragment = SensorDetailFragment.newInstance(
                sensor.getSensorType(),
                sensor.getTitle(),
                sensor.getSubtitle(),
                sensor.getEmoji()
        );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}