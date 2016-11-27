package com.idiotnation.raspored;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class Utils {

    public static final int ERROR_INTERNET = 1;
    public static final int ERROR_INTERNAL = 2;
    public static final int INFO_MESSAGE = 3;
    public static final int ERROR_UNAVAILABLE = 4;
    public static final int INFO_FINISHED = 5;

    public static int getPagerActivePage() {
        int day = Calendar.getInstance(TimeZone.getTimeZone("Europe/Sarajevo")).get(Calendar.DAY_OF_WEEK);
        if (day == 1) {
            day = 0;
        } else {
            day -= 2;
        }
        if (Calendar.getInstance(TimeZone.getTimeZone("Europe/Sarajevo")).get(Calendar.HOUR_OF_DAY) > 19) {
            if (day == 7) {
                day = 0;
            } else {
                day += 1;
            }
        }
        return day;
    }

    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    public static int manipulateAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
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
        int px = (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

}
