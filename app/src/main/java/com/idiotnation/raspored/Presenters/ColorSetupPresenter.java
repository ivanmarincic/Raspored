package com.idiotnation.raspored.Presenters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.idiotnation.raspored.Contracts.ColorSetupContract;
import com.idiotnation.raspored.Dialogs.ColorPickerDialog;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;
import com.idiotnation.raspored.Views.MainView;

import javax.inject.Inject;

import de.hdodenhof.circleimageview.CircleImageView;
import me.priyesh.chroma.ChromaDialog;
import me.priyesh.chroma.ColorMode;
import me.priyesh.chroma.ColorSelectListener;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ColorSetupPresenter implements ColorSetupContract.Presenter {

    @Inject
    public ColorSetupPresenter() {};

    ColorSetupContract.View view;
    Activity activity;

    @Override
    public void start(ColorSetupContract.View view, Activity activity) {
        this.view = view;
        this.activity = activity;
        view.initialize();
    }

    @Override
    public void populateColorsContainer(LinearLayout container, final Integer[] colorIds, final FragmentManager fragmentManager) {
        final SharedPreferences prefs = activity.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
        container.removeAllViews();
        for(int i=0; i<colorIds.length; i++){
            final String colorIdName = activity.getResources().getResourceName(colorIds[i]);
            final int color = Utils.getColor(colorIds[i], activity);
            int borderColor = (0xFFFFFF - Utils.getColor(R.color.windowBackgroundColor, activity)) | 0xFF000000;
            CircleImageView circleImageView = new CircleImageView(activity);
            circleImageView.setBorderColor(borderColor);
            circleImageView.setBorderWidth(Utils.convertDpToPixel(1, activity));
            circleImageView.setImageDrawable(new ColorDrawable(color));
            circleImageView.setLayoutParams(new RelativeLayout.LayoutParams(Utils.convertDpToPixel(50, activity), Utils.convertDpToPixel(50, activity)));
            circleImageView.setPadding(Utils.convertDpToPixel(5, activity), Utils.convertDpToPixel(5, activity), Utils.convertDpToPixel(5, activity), Utils.convertDpToPixel(5, activity));
            circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ColorPickerDialog colorPickerDialog = new ColorPickerDialog(activity, color);
                    colorPickerDialog.show();
                    colorPickerDialog.setOnFinishListener(new ColorPickerDialog.onFinishListener() {
                        @Override
                        public void onFinish(int color) {
                            prefs.edit().putInt(colorIdName, color).apply();
                            view.refreshList();
                        }
                    });
                    /*new ChromaDialog.Builder()
                            .initialColor(color)
                            .colorMode(ColorMode.ARGB)
                            .onColorSelected(new ColorSelectListener() {
                                @Override
                                public void onColorSelected(@ColorInt int color) {
                                    prefs.edit().putInt(colorIdName, color).apply();
                                    view.refreshList();
                                }
                            })
                            .create()
                            .show(fragmentManager, "ChromaDialog");*/
                }
            });
            container.addView(circleImageView);
        }
    }

    @Override
    public void save() {
        Intent intent = new Intent(activity, MainView.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

}
