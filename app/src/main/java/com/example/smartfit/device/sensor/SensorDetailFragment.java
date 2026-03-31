package com.example.smartfit.device.sensor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.smartfit.R;
import com.example.smartfit.device.collectors.SensorCollector;
import com.example.smartfit.device.model.SensorReading;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.Locale;

public class SensorDetailFragment extends Fragment {

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_SENSOR_TITLE = "sensor_title";
    private static final String ARG_SENSOR_SUBTITLE = "sensor_subtitle";
    private static final String ARG_SENSOR_EMOJI = "sensor_emoji";

    private static final int MAX_VISIBLE_POINTS = 30;

    private int sensorType;
    private String sensorTitle;
    private String sensorSubtitle;
    private String sensorEmoji;

    private TextView tvSensorName;
    private TextView tvSensorSubtitle;
    private TextView tvSensorCategory;

    private View layoutMotionSensor;
    private TextView tvValueX;
    private TextView tvValueY;
    private TextView tvValueZ;
    private TextView tvMotionInterpretation;
    private LineChart chart;

    private View layoutSingleValueSensor;
    private TextView tvSingleValueLabel;
    private TextView tvSingleValue;
    private TextView tvSingleStatus;

    private LineDataSet xDataSet;
    private LineDataSet yDataSet;
    private LineDataSet zDataSet;

    private SensorCollector sensorCollector;
    private int timeIndex = 0;

    public static SensorDetailFragment newInstance(
            int sensorType,
            String sensorTitle,
            String sensorSubtitle,
            String sensorEmoji
    ) {
        SensorDetailFragment fragment = new SensorDetailFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_SENSOR_TYPE, sensorType);
        args.putString(ARG_SENSOR_TITLE, sensorTitle);
        args.putString(ARG_SENSOR_SUBTITLE, sensorSubtitle);
        args.putString(ARG_SENSOR_EMOJI, sensorEmoji);
        fragment.setArguments(args);

        return fragment;
    }

    public SensorDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            sensorType = args.getInt(ARG_SENSOR_TYPE);
            sensorTitle = args.getString(ARG_SENSOR_TITLE, "Sensor");
            sensorSubtitle = args.getString(ARG_SENSOR_SUBTITLE, "");
            sensorEmoji = args.getString(ARG_SENSOR_EMOJI, "📈");
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_sensor_detail, container, false);

        bindViews(view);
        bindHeader();
        setupUiMode();
        setupChartIfNeeded();
        setupCollector();

        return view;
    }

    private void bindViews(View view) {
        tvSensorName = view.findViewById(R.id.tv_sensor_name);
        tvSensorSubtitle = view.findViewById(R.id.tv_sensor_subtitle);
        tvSensorCategory = view.findViewById(R.id.tv_sensor_category);

        layoutMotionSensor = view.findViewById(R.id.layout_motion_sensor);
        tvValueX = view.findViewById(R.id.tv_value_x);
        tvValueY = view.findViewById(R.id.tv_value_y);
        tvValueZ = view.findViewById(R.id.tv_value_z);
        tvMotionInterpretation = view.findViewById(R.id.tv_motion_interpretation);
        chart = view.findViewById(R.id.sensor_chart);

        layoutSingleValueSensor = view.findViewById(R.id.layout_single_value_sensor);
        tvSingleValueLabel = view.findViewById(R.id.tv_single_value_label);
        tvSingleValue = view.findViewById(R.id.tv_single_value);
        tvSingleStatus = view.findViewById(R.id.tv_single_status);
    }

    private void bindHeader() {
        tvSensorName.setText(sensorEmoji + " " + sensorTitle);
        tvSensorSubtitle.setText(sensorSubtitle);
        tvSensorCategory.setText(SensorCollector.getSensorCategoryLabel(sensorType));
    }

    private void setupUiMode() {
        boolean isMotion = SensorCollector.isMotionSensor(sensorType);
        boolean isSingleValue = SensorCollector.isSingleValueSensor(sensorType);

        if (isMotion) {
            layoutMotionSensor.setVisibility(View.VISIBLE);
            layoutSingleValueSensor.setVisibility(View.GONE);
        } else if (isSingleValue) {
            layoutMotionSensor.setVisibility(View.GONE);
            layoutSingleValueSensor.setVisibility(View.VISIBLE);
            setupSingleValueLabels();
        } else {
            layoutMotionSensor.setVisibility(View.VISIBLE);
            layoutSingleValueSensor.setVisibility(View.GONE);
        }
    }

    private void setupSingleValueLabels() {
        if (sensorType == android.hardware.Sensor.TYPE_STEP_COUNTER) {
            tvSingleValueLabel.setText("Steps");
            tvSingleStatus.setText("Waiting for step data...");
        } else if (sensorType == android.hardware.Sensor.TYPE_PROXIMITY) {
            tvSingleValueLabel.setText("Distance");
            tvSingleStatus.setText("Waiting for proximity data...");
        } else {
            tvSingleValueLabel.setText("Current Value");
            tvSingleStatus.setText("Waiting for sensor value...");
        }
    }

    private void setupChartIfNeeded() {
        if (!SensorCollector.shouldShowGraph(sensorType)) {
            return;
        }

        xDataSet = new LineDataSet(null, "X");
        yDataSet = new LineDataSet(null, "Y");
        zDataSet = new LineDataSet(null, "Z");

        xDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.cat_blue));
        yDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.cat_green));
        zDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.cat_orange));

        styleDataSet(xDataSet);
        styleDataSet(yDataSet);
        styleDataSet(zDataSet);

        LineData lineData = new LineData();
        lineData.addDataSet(xDataSet);
        lineData.addDataSet(yDataSet);
        lineData.addDataSet(zDataSet);

        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.setTouchEnabled(false);
        chart.setPinchZoom(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(android.graphics.Color.WHITE);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.invalidate();
    }

    private void styleDataSet(LineDataSet dataSet) {
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
    }

    private void setupCollector() {
        sensorCollector = new SensorCollector(
                requireContext(),
                sensorType,
                new SensorCollector.Listener() {
                    @Override
                    public void onSensorUpdate(SensorReading reading) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> updateUi(reading));
                    }

                    @Override
                    public void onSensorUnavailable() {
                        if (!isAdded()) return;

                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(
                                    requireContext(),
                                    sensorTitle + " is not available on this device",
                                    Toast.LENGTH_SHORT
                            ).show();

                            if (SensorCollector.isSingleValueSensor(sensorType)) {
                                tvSingleValue.setText("--");
                                tvSingleStatus.setText("Sensor unavailable");
                            } else {
                                tvValueX.setText("--");
                                tvValueY.setText("--");
                                tvValueZ.setText("--");
                                tvMotionInterpretation.setText("Sensor unavailable");
                            }
                        });
                    }
                }
        );
    }

    private void updateUi(SensorReading reading) {
        if (SensorCollector.isSingleValueSensor(sensorType)) {
            updateSingleValueUi(reading);
        } else {
            updateMotionUi(reading);
        }
    }

    private void updateMotionUi(SensorReading reading) {
        float x = reading.getX();
        float y = reading.getY();
        float z = reading.getZ();

        tvValueX.setText(String.format(Locale.getDefault(), "%.2f", x));
        tvValueY.setText(String.format(Locale.getDefault(), "%.2f", y));
        tvValueZ.setText(String.format(Locale.getDefault(), "%.2f", z));

        tvMotionInterpretation.setText(getMotionInterpretation(x, y, z));

        updateChart(x, y, z);
    }

    private void updateSingleValueUi(SensorReading reading) {
        float value = reading.getX();
        tvSingleValue.setText(String.format(Locale.getDefault(), "%.2f", value));

        if (sensorType == android.hardware.Sensor.TYPE_PROXIMITY) {
            tvSingleStatus.setText(value <= 1f ? "Object is near" : "Object is far");
        } else if (sensorType == android.hardware.Sensor.TYPE_STEP_COUNTER) {
            tvSingleValue.setText(String.format(Locale.getDefault(), "%.0f", value));
            tvSingleStatus.setText("Total steps recorded since device boot");
        } else {
            tvSingleStatus.setText("Live sensor value updating");
        }
    }

    private String getMotionInterpretation(float x, float y, float z) {
        double magnitude = Math.sqrt((x * x) + (y * y) + (z * z));

        if (sensorType == android.hardware.Sensor.TYPE_ACCELEROMETER) {
            if (magnitude < 2) return "Mostly still";
            if (magnitude < 8) return "Light movement detected";
            return "Strong movement detected";
        }

        if (sensorType == android.hardware.Sensor.TYPE_GYROSCOPE) {
            if (magnitude < 1) return "Stable rotation";
            if (magnitude < 4) return "Moderate rotational movement";
            return "Fast rotational movement";
        }

        if (sensorType == android.hardware.Sensor.TYPE_GRAVITY) {
            if (Math.abs(x) > Math.abs(y) && Math.abs(x) > Math.abs(z)) {
                return "Tilted sideways";
            } else if (Math.abs(y) > Math.abs(x) && Math.abs(y) > Math.abs(z)) {
                return "Tilted vertically";
            } else {
                return "Device posture looks stable";
            }
        }

        if (sensorType == android.hardware.Sensor.TYPE_LINEAR_ACCELERATION) {
            if (magnitude < 1) return "Stable";
            if (magnitude < 5) return "Active motion";
            return "Strong acceleration";
        }

        return "Live motion data updating";
    }

    private void updateChart(float x, float y, float z) {
        if (!SensorCollector.shouldShowGraph(sensorType)) {
            return;
        }

        LineData data = chart.getData();
        if (data == null) return;

        data.addEntry(new Entry(timeIndex, x), 0);
        data.addEntry(new Entry(timeIndex, y), 1);
        data.addEntry(new Entry(timeIndex, z), 2);

        trimDataSet(data, 0);
        trimDataSet(data, 1);
        trimDataSet(data, 2);

        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(MAX_VISIBLE_POINTS);
        chart.moveViewToX(Math.max(0, timeIndex - MAX_VISIBLE_POINTS));
        chart.invalidate();

        timeIndex++;
    }

    private void trimDataSet(LineData data, int dataSetIndex) {
        if (data.getDataSetByIndex(dataSetIndex) == null) return;

        while (data.getDataSetByIndex(dataSetIndex).getEntryCount() > MAX_VISIBLE_POINTS) {
            data.getDataSetByIndex(dataSetIndex).removeFirst();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorCollector != null) {
            sensorCollector.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorCollector != null) {
            sensorCollector.stop();
        }
    }
}