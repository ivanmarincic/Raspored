package com.idiotnation.raspored.Modules;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.idiotnation.raspored.Objects.TableCell;
import com.idiotnation.raspored.Recievers.NotificationReceiver;
import com.idiotnation.raspored.Utils;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class NotificationLoader extends AsyncTask<Void, Void, Void> {

    Context context;
    List<List<TableCell>> columns;
    NotificationLoaderListener notificationLoaderListener;
    SharedPreferences prefs;

    public NotificationLoader(Context context, List<List<TableCell>> columns) {
        this.context = context;
        this.columns = columns;
        prefs = context.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
    }

    @Override
    protected Void doInBackground(Void... params) {
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

    public void setFinishListener(NotificationLoaderListener notificationLoaderListener) {
        this.notificationLoaderListener = notificationLoaderListener;
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
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        }
    }

    private void setupNotifications() {
        int idNumber = 2020;
        for (int i = 0; i < columns.size(); i++) {
            for (TableCell tableCell : columns.get(i)) {
                tableCell.getStart();
                scheduleNotification(tableCell.getText(), Utils.getDelayInMiliseconds(tableCell.getStart()), idNumber);
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
                alarmManager.cancel(pendingUpdateIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface NotificationLoaderListener {
        void onFinish(List<List<TableCell>> columns);
    }

}
