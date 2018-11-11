package com.idiotnation.raspored.custom;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.idiotnation.raspored.R;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

public class MaterialDialog extends Dialog {

    AppCompatImageButton closeButton;
    AppCompatTextView titleView;
    FrameLayout contentContainer;
    Activity activity;
    float heightPercent = 0.75f;
    float widthPercent = 0.75f;

    private int contentResourceId = -1;
    private String title;

    public MaterialDialog(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        super.setContentView(R.layout.material_dialog);
        closeButton = findViewById(R.id.material_dialog_toolbar_close);
        titleView = findViewById(R.id.material_dialog_toolbar_title);
        contentContainer = findViewById(R.id.material_dialog_content);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void setContentView(int layoutResID) {
        this.contentResourceId = layoutResID;
        if (contentResourceId != -1) {
            LayoutInflater
                    .from(getContext())
                    .inflate(contentResourceId, contentContainer, true);
        }

    }

    protected Activity getActivity() {
        return activity;
    }

    public View getContentView() {
        return contentContainer;
    }

    public void setTitle(String title) {
        this.title = title;
        titleView.setText(title);
    }

    public void setWidthPercent(float widthPercent) {
        this.widthPercent = widthPercent;
    }

    public void setHeightPercent(float heightPercent) {
        this.heightPercent = heightPercent;
    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        if (widthPercent != -1) {
            layoutParams.width = (int) (displayWidth * widthPercent);
        }
        if (heightPercent != -1) {
            layoutParams.height = (int) (displayHeight * heightPercent);
        }
        getWindow().setAttributes(layoutParams);
    }
}
