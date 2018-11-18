package com.idiotnation.raspored.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.models.dto.CourseDto;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsCoursesAdapter extends RecyclerView.Adapter<SettingsCoursesAdapter.ViewHolder> {

    private List<CourseDto> list;
    private ItemOnSelectListener listener;
    private Integer selectedItem;
    private Integer selectedBackground;

    public List<CourseDto> getList() {
        return list;
    }

    public void setList(List<CourseDto> list) {
        this.list = list;
    }

    public SettingsCoursesAdapter(Context context, List<CourseDto> list, Integer selectedItem) {
        this.list = list;
        this.selectedItem = selectedItem;
        selectedBackground = ContextCompat.getColor(context, R.color.colorOnBackgroundSelected);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CourseDto item = list.get(position);
        holder.value.setText(item.getName());
        if (item.getId().equals(selectedItem)) {
            holder.itemView.setBackgroundColor(selectedBackground);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSelect(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView value;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            value = itemView.findViewById(R.id.list_item_value);
        }
    }

    public interface ItemOnSelectListener {
        void onSelect(CourseDto item);
    }

    public void setItemOnSelectListener(ItemOnSelectListener listener) {
        this.listener = listener;
    }
}
