package com.idiotnation.raspored;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static final int MODE_HORIZONTAL = 0;
    public static final int MODE_VERTICAL = 1;
    public static final String lineColor = "ff000000";
    public static final int ERROR_INTERNET = 1;
    public static final int ERROR_INTERNAL = 2;
    public static final int INFO_MESSAGE = 3;
    public static final int ERROR_UNAVAILABLE = 4;
    public static final int INFO_FINISHED = 5;

    public static String getGDiskId(String url) {

        String output = "NN";
        int slash = 0;
        if (url.length() >= 32) {
            for (int i = 32; i < url.length(); i++) {
                if (url.substring(i, (i + 1)).equals("/")) {
                    slash = i;
                    break;
                }
            }
            output = url.substring(32, slash);
        }
        return output;
    }

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

    public static long getDelayInMiliseconds(Date date) {
        long diffInMillies = date.getTime() - new Date().getTime();
        return TimeUnit.MILLISECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

/*    public static ArrayList<TableColumn> getColumns(Bitmap bmp, List<Integer> xs, List<Integer> ys) {
        if (bmp != null) {
            ArrayList<TableColumn> columns = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                Point topLeftCorner = new Point(xs.get(i), ys.get(0));
                int wThickness = 0;
                if (i != 0) {
                    wThickness = getLineThickness(bmp, new Point(xs.get(i + 1), ys.get(0) + 20), MODE_HORIZONTAL);
                }
                int hThickness = getLineThickness(bmp, new Point(xs.get(0) + 20, ys.get(ys.size() - 1)), MODE_VERTICAL);
                int columnWidth = (xs.get(i + 1) - xs.get(i)) + wThickness;
                int columnHeight = (ys.get(ys.size() - 1) - ys.get(0)) + hThickness;
                columns.add(new TableColumn(topLeftCorner.x, topLeftCorner.y, columnWidth, columnHeight));
            }
            return columns;
        }
        return null;
    }*/

    public static int getLineThickness(Bitmap sourceImage, Point coord, int mode) {
        for (int i = 0; i <= 50; i++) {
            if (mode == MODE_HORIZONTAL) {
                if (!Integer.toHexString(sourceImage.getPixel(coord.x + i, coord.y)).equals(lineColor)) {
                    return i;
                }
            } else if (mode == MODE_VERTICAL) {
                if (!Integer.toHexString(sourceImage.getPixel(coord.x, coord.y + i)).equals(lineColor)) {
                    return i;
                }
            }
        }
        return 3;
    }

    public static Bitmap createInvertedBitmap(Bitmap src, boolean darkMode) {
        if (src != null) {
            if (darkMode) {
                ColorMatrix colorMatrix_Inverted =
                        new ColorMatrix(new float[]{
                                -1, 0, 0, 0, 255,
                                0, -1, 0, 0, 255,
                                0, 0, -1, 0, 255,
                                0, 0, 0, 1, 0});
                ColorFilter ColorFilter_Sepia = new ColorMatrixColorFilter(
                        colorMatrix_Inverted);
                Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                paint.setColorFilter(ColorFilter_Sepia);
                canvas.drawBitmap(src, 0, 0, paint);
                return bitmap;
            } else {
                return src;
            }
        }
        return null;
    }

    public static String getFinalURL(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        con.getInputStream();

        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = con.getHeaderField("Location");
            return getFinalURL(redirectUrl);
        }
        return url;
    }

    public static boolean checkActiveInternetConnection(Context context) {
        try {
            Jsoup.connect("https://www.google.com").timeout(3000).get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
