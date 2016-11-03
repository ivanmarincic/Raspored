package com.idiotnation.raspored;


import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;


public class ImageFragment extends Fragment {

    List<TableColumn> columns;

    public ImageFragment() {
    }

    public void setParams(List<TableColumn> columns) {
        this.columns = columns;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PercentRelativeLayout rootView = new PercentRelativeLayout(getContext());
        rootView.setBackgroundDrawable(getResources().getDrawable(R.drawable.separator));
        float scale = getResources().getDisplayMetrics().density;
        rootView.setPadding((int) (1 * scale + 0.5f), (int) (1 * scale + 0.5f), (int) (1 * scale + 0.5f), (int) (1 * scale + 0.5f));
        rootView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) {
                TextView textView = new TextView(getContext());
                textView.setGravity(Gravity.CENTER);
                textView.setText(columns.get(i).getText());
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                PercentRelativeLayout.LayoutParams params = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
                info.heightPercent = (float) ((1.0 / 13.0) * columns.get(i).height)*0.98f;
                info.widthPercent = 1f;
                info.topMarginPercent = (float) ((1.0 / 13.0) * columns.get(i).top);
                info.leftMarginPercent = 0;
                int[] attribute = new int[]{R.attr.windowBackgroundSecondary, R.attr.textColorPrimary};
                TypedArray array = getContext().getTheme().obtainStyledAttributes(attribute);
                textView.setBackgroundColor(array.getColor(0, Color.TRANSPARENT));
                textView.setTextColor(array.getColor(1, Color.TRANSPARENT));
                array.recycle();
                textView.setLayoutParams(params);
                rootView.addView(textView);
            }
        }
        return rootView;
    }
}
