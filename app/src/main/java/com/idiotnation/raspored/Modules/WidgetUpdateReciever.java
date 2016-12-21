package com.idiotnation.raspored.Modules;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idiotnation.raspored.Widget.RasporedWidgetProvider;

import java.io.File;
import java.io.FileOutputStream;

import static android.content.Context.MODE_PRIVATE;

public class WidgetUpdateReciever extends BroadcastReceiver {

    public static String UPDATE = "RasporedWidgetUpdateContent";

    @Override
    public void onReceive(Context context, Intent intent) {
        TableCell tableCell = (TableCell) new Gson().fromJson(intent.getStringExtra(UPDATE).toString(), new TypeToken<TableCell>() {
        }.getType());
        saveColumnToJson(tableCell, context);
        Intent widgetIntent = new Intent(context, RasporedWidgetProvider.class);
        context.sendBroadcast(widgetIntent);
    }

    public void saveColumnToJson(TableCell column, Context context) {
        try {
            new File(context.getFilesDir() + "/widget.json").delete();
            FileOutputStream fos = context.openFileOutput("widget.json", MODE_PRIVATE);
            fos.write(new Gson().toJson(column).getBytes());
            fos.close();
        } catch (Exception e) {
        }
    }

}
