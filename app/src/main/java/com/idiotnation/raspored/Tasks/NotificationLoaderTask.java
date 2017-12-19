package com.idiotnation.raspored.Tasks;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.idiotnation.raspored.Helpers.BackgroundTask;
import com.idiotnation.raspored.Models.LessonCell;
import com.idiotnation.raspored.Recievers.NotificationReceiver;
import com.idiotnation.raspored.Utils;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class NotificationLoaderTask extends BackgroundTask<List<List<LessonCell>>> {

    Context context;
    List<List<LessonCell>> columns;
    SharedPreferences prefs;

    public NotificationLoaderTask(Context context, List<List<LessonCell>> columns) {
        this.context = context;
        this.columns = columns;
        prefs = context.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
    }

    @Override
    protected void onCreate() {

    }

    @Override
    protected List<List<LessonCell>> onExecute() {
        try {
            clearNotifications();
            if (prefs.getBoolean("NotificationsEnabled", false)) {
                setupNotifications();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scheduleNotification(String notification, long delay, int id) {
        delay = delay - (30 * 60000);
        if (delay > 0) {
            Intent notificationIntent = new Intent(context, NotificationReceiver.class);
            notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, id);
            notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            long futureInMillis = SystemClock.elapsedRealtime() + delay;
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
            }
        }
    }

    private void setupNotifications() {
        int idNumber = 2020;
        for (int i = 0; i < columns.size(); i++) {
            for (LessonCell lessonCell : columns.get(i)) {
                scheduleNotification(lessonCell.getText(), Utils.getDelayInMiliseconds(lessonCell.getStart()), idNumber);
                idNumber++;
            }
        }
    }

    public void clearNotifications() {
        for (int i = 0; i < 160; i++) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent updateServiceIntent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(context, 2020 + i, updateServiceIntent, 0);

            try {
                if (alarmManager != null) {
                    alarmManager.cancel(pendingUpdateIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
