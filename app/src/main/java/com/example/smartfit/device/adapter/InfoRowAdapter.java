package com.example.smartfit.device.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfit.R;
import com.example.smartfit.device.model.InfoRow;

import java.util.List;
public class InfoRowAdapter extends RecyclerView.Adapter<InfoRowAdapter.InfoRowViewHolder> {

    public final List<InfoRow> rows;

    public InfoRowAdapter(List<InfoRow> rows){
        this.rows = rows;
    }

    @NonNull
    @Override
    public InfoRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_info_row, parent, false);
        return new InfoRowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoRowViewHolder holder, int position){
        InfoRow row = rows.get(position);
        holder.tvKey.setText(row.getKey());
        holder.tvValue.setText(row.getValue());
    }

    @Override
    public int getItemCount(){
        return rows.size();
    }

    static class InfoRowViewHolder extends RecyclerView.ViewHolder{
        TextView tvKey,tvValue;

        public InfoRowViewHolder(@NonNull View itemView){
            super(itemView);
            tvKey = itemView.findViewById(R.id.tv_info_key);
            tvValue = itemView.findViewById(R.id.tv_info_value);
        }
    }

}
