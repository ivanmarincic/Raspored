package com.idiotnation.raspored.Modules;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.idiotnation.raspored.Objects.TableCell;
import com.idiotnation.raspored.Presenters.MainPresenter;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;
import com.idiotnation.raspored.Widget.RasporedWidgetProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class UpdateWidgetService extends Service {

    MainPresenter presenter;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        presenter = new MainPresenter();
        presenter.start(null, getApplicationContext());

        try {
            WidgetData widgetData = getAppropriateItem(presenter.getRaspored(), allWidgetIds);
            TableCell tableCell = widgetData.tableCell;
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm");

            if (tableCell != null) {
                for (int widgetId : allWidgetIds) {
                    RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.raspored_widget_layout);
                    remoteViews.setTextViewText(R.id.widget_text_content, tableCell.getText());
                    remoteViews.setTextColor(R.id.widget_text_content, Utils.getColor(R.color.widgetTextColorPrimary, getApplicationContext()));
                    if (tableCell.getStart() != null || tableCell.getEnd() != null) {
                        remoteViews.setTextViewText(R.id.widget_text_time, getDayOfWeek(tableCell.getStart()) + "  " + simpleTimeFormat.format(tableCell.getStart()) + " - " + simpleTimeFormat.format(tableCell.getEnd()));
                    } else {
                        remoteViews.setTextViewText(R.id.widget_text_time, "");
                    }
                    remoteViews.setTextColor(R.id.widget_text_time, Utils.getColor(R.color.widgetTextColorPrimary, getApplicationContext()));
                    remoteViews.setTextViewText(R.id.widget_text_exams, "Ispiti: " + widgetData.numberOfExams + "/" + widgetData.numberOfLessons);
                    remoteViews.setTextColor(R.id.widget_text_exams, Utils.getColor(R.color.widgetTextColorPrimary, getApplicationContext()));
                    remoteViews.setInt(R.id.widget_text_container, "setBackgroundColor", Utils.getColor(R.color.widgetBackgroundColor, getApplicationContext()));
                    Intent clickIntent = new Intent(getApplicationContext(), RasporedWidgetProvider.class);
                    clickIntent.putExtra(Utils.WIDGET_INTENT, Utils.WIDGET_CLICK);
                    PendingIntent clickPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), Utils.WIDGET_CLICK_INTENT, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.widget_text_container, clickPendingIntent);
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopSelf();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private WidgetData getAppropriateItem(List<List<TableCell>> columns, int[] allWidgetIds) {
        List<TableCell> lessons = Utils.shrinkList(columns);
        WidgetData widgetData = new WidgetData();
        widgetData.numberOfExams = 0;
        widgetData.numberOfLessons = lessons.size();
        boolean isCellFound = false;
        for (int i = 0; i < lessons.size(); i++) {
            TableCell tableCell = lessons.get(i);
            if (tableCell.getText().contains("kolokvij") || tableCell.getText().contains("ispit")) {
                widgetData.numberOfExams++;
            }
            if (tableCell.getStart().compareTo(new Date()) > 0 && !isCellFound) {
                if (0 < i) {
                    scheduleWidgetUpdate(Utils.getDelayInMiliseconds(tableCell.getStart()), allWidgetIds);
                } else {
                    scheduleWidgetUpdate(0, allWidgetIds);
                }
                widgetData.tableCell = tableCell;
                isCellFound = true;
            }
        }
        if (widgetData.tableCell == null) {
            TableCell tableCell = new TableCell();
            tableCell.setText("Nema predavanja");
            widgetData.tableCell = tableCell;
        }
        return widgetData;
    }

    private void scheduleWidgetUpdate(long delay, int[] allWidgetIds) {
        Intent intent = new Intent(UpdateWidgetService.this, UpdateWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getService(UpdateWidgetService.this, Utils.UNIQUE_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private String getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Sarajevo"));
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY:
                return "Nedjelja";
            case Calendar.MONDAY:
                return "Ponedjeljak";
            case Calendar.TUESDAY:
                return "Utorak";
            case Calendar.WEDNESDAY:
                return "Srijeda";
            case Calendar.THURSDAY:
                return "Četvrtak";
            case Calendar.FRIDAY:
                return "Petak";
            case Calendar.SATURDAY:
                return "Subota";
        }
        return new SimpleDateFormat("dd.MM").format(date);
    }

    private class WidgetData {

        public TableCell tableCell;
        public int numberOfExams;
        public int numberOfLessons;

        public WidgetData() {
        }
    }

}
