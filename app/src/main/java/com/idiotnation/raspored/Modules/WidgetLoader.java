package com.idiotnation.raspored.Modules;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.idiotnation.raspored.Utils;
import com.idiotnation.raspored.Widget.RasporedWidgetProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class WidgetLoader extends AsyncTask<Void, Void, Void> {

    Context context;
    List<List<TableCell>> columns;
    NotificationLoaderListener notificationLoaderListener;
    SharedPreferences prefs;

    public WidgetLoader(Context context, List<List<TableCell>> columns) {
        this.context = context;
        this.columns = columns;
        prefs = context.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            clearWidgetUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setFinishListener(NotificationLoaderListener notificationLoaderListener) {
        this.notificationLoaderListener = notificationLoaderListener;
    }

    public interface NotificationLoaderListener {
        void onFinish(List<List<TableCell>> columns);
    }

    private void scheduleWidgetUpdate(int content, long delay, int id) {
        Intent intent = new Intent(context, WidgetUpdateReciever.class);
        intent.putExtra(WidgetUpdateReciever.UPDATE, content);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private void setupWidgetUpdate() {
        int idNumber = 4020;
        List<TableCell> lessons = populateLessonsList(columns);
        for (int i = lessons.size() - 1; i >= 0; i--) {
            TableCell tableColumn1 = lessons.get(i);
            if (tableColumn1.isVisible() && tableColumn1.getStart().compareTo(new Date()) > 0) {
                if (0 < i) {
                    TableCell tableColumn2 = lessons.get(i - 1);
                    scheduleWidgetUpdate(i, Utils.getDelayInMiliseconds(tableColumn2.getStart()), idNumber);
                } else {
                    scheduleWidgetUpdate(i, 0, idNumber);
                }
            }
            idNumber++;
        }
    }

    public void clearWidgetUpdates() {
        for (int i = 0; i < 160; i++) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent updateServiceIntent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(context, 4020 + i, updateServiceIntent, 0);

            try {
                alarmManager.cancel(pendingUpdateIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<TableCell> populateLessonsList(List<List<TableCell>> columns) {
        List<TableCell> lessons = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            for (int j = 0; j < columns.get(i).size(); j++) {
                lessons.add(columns.get(i).get(j));
            }
        }
        return lessons;
    }

}
