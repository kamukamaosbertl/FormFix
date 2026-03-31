package com.example.smartfit.device.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfit.R;
import com.example.smartfit.device.model.InfoCategory;

import java.util.List;

public class DeviceCategoryAdapter extends RecyclerView.Adapter<DeviceCategoryAdapter.CategoryViewHolder> {

    private final List<InfoCategory> categories;

    public DeviceCategoryAdapter(List<InfoCategory> categories){
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_info_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        InfoCategory category = categories.get(position);
        Context context = holder.itemView.getContext();

        holder.tvCategoryTitle.setText(category.getTitle());

        @ColorInt int color = ContextCompat.getColor(context, category.getHeaderColorRes());
        holder.tvCategoryTitle.setBackgroundColor(color);

        holder.recyclerRows.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerRows.setNestedScrollingEnabled(false);
        holder.recyclerRows.setAdapter(new InfoRowAdapter(category.getRows()));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryTitle;
        RecyclerView recyclerRows;

        public CategoryViewHolder(@NonNull View itemView){
            super(itemView);
            tvCategoryTitle = itemView.findViewById(R.id.tv_category_title);
            recyclerRows = itemView.findViewById(R.id.recycler_info_rows);
        }

    }
}
