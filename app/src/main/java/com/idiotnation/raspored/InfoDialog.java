package com.idiotnation.raspored;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_dialog);
        textContent = (TextView) findViewById(R.id.text_content);
        textTime = (TextView) findViewById(R.id.text_time);
        textContent.setText(tableColumn.getText());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        textTime.setText(sdf.format(tableColumn.getStart()) + " - " + sdf.format(tableColumn.getEnd()));
    }
}
