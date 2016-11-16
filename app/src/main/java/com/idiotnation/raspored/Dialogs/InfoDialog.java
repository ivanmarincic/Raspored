package com.idiotnation.raspored.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Modules.TableColumn;

import java.text.SimpleDateFormat;


public class InfoDialog extends Dialog {

    private TableColumn tableColumn;
    private TextView textContent, textTime;

    public InfoDialog(Context context, TableColumn tableColumn) {
        super(context);
        this.tableColumn = tableColumn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_dialog);
        textContent = (TextView) findViewById(R.id.text_content);
        textTime = (TextView) findViewById(R.id.text_time);
        textContent.setText(tableColumn.getText());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        textTime.setText(sdf.format(tableColumn.getStart()) + " - " + sdf.format(tableColumn.getEnd()));
    }
}
