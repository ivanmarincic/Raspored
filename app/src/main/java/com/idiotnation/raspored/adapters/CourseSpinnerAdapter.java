package com.idiotnation.raspored.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.models.dto.CourseDto;

import java.util.List;

import androidx.appcompat.widget.AppCompatTextView;

public class CourseSpinnerAdapter extends ArrayAdapter<CourseDto> {
    public CourseSpinnerAdapter(Context context, int resource, List<CourseDto> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_selected_item, parent, false);
        }
        CourseDto item = getItem(position);
        AppCompatTextView textView = convertView.findViewById(android.R.id.text1);
        if (item != null) {
            textView.setText(item.getName());
        } else {
            textView.setText(parent.getContext().getResources().getString(R.string.spinner_default));
        }
        textView.setSelected(true);
        return convertView;
    }
}
