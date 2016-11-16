package com.idiotnation.raspored.Widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.RemoteViews;

import com.idiotnation.raspored.Modules.TableColumn;
import com.idiotnation.raspored.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RasporedWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context,
                RasporedWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.raspored_widget_layout);
            remoteViews.setTextViewText(R.id.widget_text_content, "TEST");
            remoteViews.setTextViewText(R.id.widget_text_time, "00:00-00:00");
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }
}
