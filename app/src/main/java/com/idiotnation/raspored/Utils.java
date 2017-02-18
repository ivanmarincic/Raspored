package com.idiotnation.raspored;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class Utils {

    public static final int ERROR_INTERNET = 1;
    public static final int ERROR_INTERNAL = 2;
    public static final int INFO_MESSAGE = 3;
    public static final int ERROR_UNAVAILABLE = 4;
    public static final int INFO_FINISHED = 5;
    public static final String WIDGET_ACTIVE = "com.idiotnation.RasporedWidgetActive";
    public static final int UNIQUE_ID = 29109613;
    public static final int WIDGET_CLICK_INTENT = 29109666;
    public static final String WIDGET_INTENT = "com.idiotnation.RasporedWidgetIntent";
    public static final String WIDGET_UPDATE = "com.idiotnation.RasporedWidgetUpdate";
    public static final String WIDGET_CLICK = "com.idiotnation.RasporedWidgetClick";

    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }

    public static int manipulateAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static <T> List<T> shrinkList(List<List<T>> shrink) {
        List<T> list = new ArrayList<>();
        for(int i=0; i<shrink.size(); i++){
            list.addAll(shrink.get(i));
        }
        return list;
    }

    public static long getDelayInMiliseconds(Date date) {
        long diffInMillies = date.getTime() - new Date().getTime();
        return TimeUnit.MILLISECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public static int getColor(int colorId, Context context) {
        return context.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE).getInt(context.getResources().getResourceName(colorId), context.getResources().getColor(colorId));
    }

    public static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static boolean isWidgetActive(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(WIDGET_ACTIVE, false);
    }

}
