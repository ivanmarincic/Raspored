package com.idiotnation.raspored.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idiotnation.raspored.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class RemovableArrayListAdapter extends RecyclerView.Adapter<RemovableArrayListAdapter.ViewHolder> {

    private List list;
    private ItemOnRemoveListener listener;

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public RemovableArrayListAdapter(List list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_removable, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Object item = list.get(position);
        holder.name.setText(item.toString());
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = list.indexOf(item);
                list.remove(index);
                notifyItemRemoved(index);
                if (listener != null) {
                    listener.onRemove(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView name;
        AppCompatImageButton remove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.list_item_removable_name);
            remove = itemView.findViewById(R.id.list_item_removable_remove);
        }
    }

    public interface ItemOnRemoveListener {
        void onRemove(Object item);
    }

    public void setItemOnRemoveListener(ItemOnRemoveListener listener) {
        this.listener = listener;
    }
}
