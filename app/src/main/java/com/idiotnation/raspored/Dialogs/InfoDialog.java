package com.idiotnation.raspored.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.idiotnation.raspored.Models.LessonCell;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;

import java.text.SimpleDateFormat;


public class InfoDialog extends Dialog {

    private LessonCell lessonCell;

    public InfoDialog(Context context, LessonCell lessonCell) {
        super(context);
        this.lessonCell = lessonCell;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_dialog);
        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.info_dialog_bg);
        rootView.setBackgroundColor(Utils.getColor(R.color.windowBackgroundColor, getContext()));
        TextView textContent = (TextView) findViewById(R.id.text_content);
        textContent.setTextColor(Utils.getColor(R.color.textColorPrimary, getContext()));
        TextView textTime = (TextView) findViewById(R.id.text_time);
        textTime.setTextColor(Utils.getColor(R.color.textColorPrimary, getContext()));
        textContent.setText(lessonCell.getText());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        textTime.setText(sdf.format(lessonCell.getStart()) + " - " + sdf.format(lessonCell.getEnd()));
    }
}
