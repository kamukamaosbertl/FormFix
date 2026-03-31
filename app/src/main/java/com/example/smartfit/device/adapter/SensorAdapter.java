package com.example.smartfit.device.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfit.R;
import com.example.smartfit.device.model.SensorSnapshot;
import com.example.smartfit.device.model.SensorSnapshot;

import java.util.List;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.ViewHolder> {

    public interface OnSensorClickListener {
        void onSensorClick(SensorSnapshot sensor);
    }

    private final List<SensorSnapshot> sensors;
    private final OnSensorClickListener listener;

    public SensorAdapter(List<SensorSnapshot> sensors, OnSensorClickListener listener) {
        this.sensors = sensors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sensor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SensorSnapshot sensor = sensors.get(position);

        holder.tvEmoji.setText(sensor.getEmoji());
        holder.tvName.setText(sensor.getTitle());
        holder.tvSubtitle.setText(sensor.getSubtitle());

        holder.itemView.setOnClickListener(v -> listener.onSensorClick(sensor));
    }

    @Override
    public int getItemCount() {
        return sensors.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvSubtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tv_sensor_emoji);
            tvName = itemView.findViewById(R.id.tv_sensor_name);
            tvSubtitle = itemView.findViewById(R.id.tv_sensor_subtitle);
        }
    }
}