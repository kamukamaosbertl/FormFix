package com.example.smartfit.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.smartfit.R;
import com.example.smartfit.data.model.DashboardItem;
import com.example.smartfit.ui.common.DashboardCardClickListener;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private final List<DashboardItem> items;
    private final DashboardCardClickListener listener;

    public DashboardAdapter(List<DashboardItem> items, DashboardCardClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardItem item = items.get(position);

        holder.ivIcon.setImageResource(item.getIconResId());
        holder.ivIcon.setColorFilter(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.cat_blue)
        );

        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());

        holder.card.setOnClickListener(v -> listener.onDashboardItemClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        AppCompatImageView ivIcon;
        TextView tvTitle;
        TextView tvSubtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_dashboard_item);
            ivIcon = itemView.findViewById(R.id.iv_dashboard_icon);
            tvTitle = itemView.findViewById(R.id.tv_dashboard_title);
            tvSubtitle = itemView.findViewById(R.id.tv_dashboard_subtitle);
        }
    }
}