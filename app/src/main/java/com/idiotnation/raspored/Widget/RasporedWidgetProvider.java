package com.idiotnation.raspored.Widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idiotnation.raspored.Modules.TableCell;
import com.idiotnation.raspored.Modules.UpdateWidgetService;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class RasporedWidgetProvider extends AppWidgetProvider {

    public static String UPDATE_ID = "RasporedWidgetUpdate";
    public static String UPDATE = "RasporedWidgetUpdateContent";
    String content = "";
    Date start, end;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context,
                RasporedWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        Intent intent = new Intent(context.getApplicationContext(),
                UpdateWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        context.startService(intent);
    }

    private void setWidgetActive(boolean active, Context context){
        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(Utils.WIDGET_ACTIVE, active);
        edit.commit();
    }

    @Override
    public void onEnabled(Context context) {
        setWidgetActive(true, context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        setWidgetActive(false, context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        setWidgetActive(false, context);
    }
}
