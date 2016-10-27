package com.idiotnation.raspored;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;


public class ImageFragment extends Fragment {

    int dayNum;
    Bitmap sourceImage;
    List<TableColumn> columns;

    public ImageFragment() {
    }

    public void setParams(int dayNum, Bitmap sourceImage, List<TableColumn> columns){
        this.dayNum = dayNum+1;
        this.sourceImage = sourceImage;
        this.columns = columns;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_layout, container, false);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.column);
        if (columns != null && sourceImage != null) {
            imageView.setImageBitmap(Bitmap.createBitmap(sourceImage, columns.get(dayNum).getX(), columns.get(dayNum).getY(), columns.get(dayNum).getWidth(), columns.get(dayNum).getHeight()));
        }
        return rootView;
    }
}
