package com.idiotnation.raspored.Widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.idiotnation.raspored.Services.WidgetUpdateService;
import com.idiotnation.raspored.Utils;

public class RasporedWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context,
                RasporedWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        Intent intent = new Intent(context.getApplicationContext(),
                WidgetUpdateService.class);
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
    public void onReceive(Context context, Intent intent) {
        String intentExtra = intent.getStringExtra(Utils.WIDGET_INTENT);
        if (intentExtra != null) {
            if (intentExtra.equals(Utils.WIDGET_UPDATE)) {
                int[] appWidgetIds = new int[]{};
                this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
            } else if (intentExtra.equals(Utils.WIDGET_CLICK)) {
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.idiotnation.raspored"));
            }
        } else {
            super.onReceive(context, intent);
        }
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
