package com.idiotnation.raspored.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.idiotnation.raspored.Modules.TableCell;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;

import java.text.SimpleDateFormat;


public class InfoDialog extends Dialog {

    private TableCell tableCell;
    private TextView textContent, textTime;

    public InfoDialog(Context context, TableCell tableCell) {
        super(context);
        this.tableCell = tableCell;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_dialog);
        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.info_dialog_bg);
        rootView.setBackgroundColor(Utils.getColor(R.color.windowBackgroundColor, getContext()));
        textContent = (TextView) findViewById(R.id.text_content);
        textContent.setTextColor(Utils.getColor(R.color.textColorPrimary, getContext()));
        textTime = (TextView) findViewById(R.id.text_time);
        textTime.setTextColor(Utils.getColor(R.color.textColorPrimary, getContext()));
        textContent.setText(tableCell.getText());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        textTime.setText(sdf.format(tableCell.getStart()) + " - " + sdf.format(tableCell.getEnd()));
    }
}
