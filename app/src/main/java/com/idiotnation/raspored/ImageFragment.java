package com.idiotnation.raspored;


import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final RelativeLayout rootView = new RelativeLayout(getContext());
        rootView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (columns != null) {
            rootView.post(new Runnable() {
                @Override
                public void run() {
                    float scale = getResources().getDisplayMetrics().density;
                    int[] attribute = new int[]{R.attr.textColorPrimary, R.attr.windowBackgroundSecondary, R.attr.dialogBackgroundSecondary};
                    TypedArray array = getContext().getTheme().obtainStyledAttributes(attribute);
                    GradientDrawable textViewBg = (GradientDrawable) getResources().getDrawable(R.drawable.separator).getConstantState().newDrawable();
                    textViewBg.setStroke((int) (1 * scale + 0.5f), array.getColor(2, Color.TRANSPARENT));
                    textViewBg.setColor(array.getColor(1, Color.TRANSPARENT));
                    for (int i = 0; i < columns.size(); i++) {
                        TextView textView = new TextView(getContext());
                        textView.setGravity(Gravity.CENTER);
                        textView.setTypeface(Typeface.DEFAULT_BOLD);
                        textView.setTextColor(array.getColor(0, Color.TRANSPARENT));
                        textView.setBackgroundDrawable(textViewBg);
                        textView.setMaxLines(2);
                        int padding = columns.get(i).getColCount()>1?(int) (rootView.getWidth()*0.01f):(int) (rootView.getWidth()*0.05f);
                        textView.setPadding(padding, padding, padding, padding);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, i == 12 ? ViewGroup.LayoutParams.MATCH_PARENT : rootView.getHeight() / 13);
                        params.topMargin = (int)((rootView.getHeight() / 13) * columns.get(i).getTop());
                        params.leftMargin = (int)((rootView.getWidth() / columns.get(i).getColCount()) * (columns.get(i).getLeft()-1));
                        params.height = (int)((rootView.getHeight() / 13) * columns.get(i).getHeight());
                        params.width = (rootView.getWidth() / columns.get(i).getColCount()) * columns.get(i).getWidth();
                        textView.setLayoutParams(params);
                        final int current = i;
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                InfoDialog infoDialog = new InfoDialog(getActivity(), columns.get(current));
                                infoDialog.show();
                            }
                        });
                        textView.setText(columns.get(i).getText());
                        rootView.addView(textView);
                    }
                    array.recycle();
                }
            });
        }
        return rootView;
    }
}
