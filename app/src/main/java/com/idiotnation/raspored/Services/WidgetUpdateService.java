package com.idiotnation.raspored.Services;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.idiotnation.raspored.Models.LessonCell;
import com.idiotnation.raspored.Presenters.MainPresenter;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;
import com.idiotnation.raspored.Widget.RasporedWidgetProvider;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

public class WidgetUpdateService extends Service {

    MainPresenter presenter;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        presenter = new MainPresenter();
        presenter.start(null, getApplicationContext());

        try {
            WidgetData widgetData = getAppropriateItem(presenter.getRaspored(), allWidgetIds);
            LessonCell lessonCell = widgetData.lessonCell;
            DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

            if (lessonCell != null) {
                for (int widgetId : allWidgetIds) {
                    RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.raspored_widget_layout);
                    remoteViews.setTextViewText(R.id.widget_text_content, lessonCell.getText());
                    remoteViews.setTextColor(R.id.widget_text_content, Utils.getColor(R.color.widgetTextColorPrimary, getApplicationContext()));
                    if (lessonCell.getStart() != null || lessonCell.getEnd() != null) {
                        remoteViews.setTextViewText(R.id.widget_text_time, getDayOfWeek(lessonCell.getStart()) + "  " + timeFormatter.print(lessonCell.getStart()) + " - " + timeFormatter.print(lessonCell.getEnd()));
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

    private WidgetData getAppropriateItem(List<List<LessonCell>> columns, int[] allWidgetIds) {
        List<LessonCell> lessons = Utils.shrinkList(columns);
        WidgetData widgetData = new WidgetData();
        widgetData.numberOfExams = 0;
        widgetData.numberOfLessons = lessons.size();
        boolean isCellFound = false;
        for (int i = 0; i < lessons.size(); i++) {
            LessonCell lessonCell = lessons.get(i);
            if (lessonCell.getText().contains("kolokvij") || lessonCell.getText().contains("ispit")) {
                widgetData.numberOfExams++;
            }
            if (lessonCell.getStart().isAfterNow() && !isCellFound) {
                if (0 < i) {
                    scheduleWidgetUpdate(Utils.getDelayInMiliseconds(lessonCell.getStart()), allWidgetIds);
                } else {
                    scheduleWidgetUpdate(0, allWidgetIds);
                }
                widgetData.lessonCell = lessonCell;
                isCellFound = true;
            }
        }
        if (widgetData.lessonCell == null) {
            LessonCell lessonCell = new LessonCell();
            lessonCell.setText("Nema predavanja");
            widgetData.lessonCell = lessonCell;
        }
        return widgetData;
    }

    private void scheduleWidgetUpdate(long delay, int[] allWidgetIds) {
        Intent intent = new Intent(WidgetUpdateService.this, WidgetUpdateService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getService(WidgetUpdateService.this, Utils.UNIQUE_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private String getDayOfWeek(DateTime date) {
        switch (date.getDayOfWeek()) {
            case 7:
                return "Nedjelja";
            case 1:
                return "Ponedjeljak";
            case 2:
                return "Utorak";
            case 3:
                return "Srijeda";
            case 4:
                return "Četvrtak";
            case 5:
                return "Petak";
            case 6:
                return "Subota";
            default:
                return "";
        }
    }

    private class WidgetData {

        public LessonCell lessonCell;
        public int numberOfExams;
        public int numberOfLessons;

        public WidgetData() {
        }
    }

}
