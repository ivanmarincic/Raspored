package com.idiotnation.raspored.Modules;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.idiotnation.raspored.Utils;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static android.support.v4.app.NotificationCompat.DEFAULT_LIGHTS;
import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;
import static com.idiotnation.raspored.R.drawable.notification_icon;

public class NotificationLoader extends AsyncTask<Void, Void, Void> {

    Context context;
    List<List<TableColumn>> columns;
    NotificationLoaderListener notificationLoaderListener;
    SharedPreferences prefs;

    public NotificationLoader(Context context, List<List<TableColumn>> columns) {
        this.context = context;
        this.columns = columns;
        prefs = context.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            clearNotifications();
            if(prefs.getBoolean("NotificationsEnabled", false)){
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

    public interface NotificationLoaderListener {
        void onFinish(List<List<TableColumn>> columns);
    }

    private Notification getLessonNotification(String text){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE | DEFAULT_LIGHTS)
                        .setSmallIcon(notification_icon)
                        .setContentTitle("Predavanje")
                        .setContentText(text + " za pola sata");
        return mBuilder.build();
    }

    private void scheduleNotification(Notification notification, long delay, int id) {
        delay = delay - (30*600000);
        if(delay>0){
            Intent notificationIntent = new Intent(context, NotificationReciever.class);
            notificationIntent.putExtra(NotificationReciever.NOTIFICATION_ID, id);
            notificationIntent.putExtra(NotificationReciever.NOTIFICATION, notification);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            long futureInMillis = SystemClock.elapsedRealtime() + delay;
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        }
    }

    private void setupNotifications(){
        int idNumber = 2020;
        for(int i=0; i< columns.size(); i++){
            for (TableColumn tableColumn : columns.get(i)){
                tableColumn.getStart();
                scheduleNotification(getLessonNotification(tableColumn.getText()), Utils.getDelayInMiliseconds(tableColumn.getStart()), idNumber);
                idNumber++;
            }
        }
    }

    public void clearNotifications(){
        for(int i=0; i<160; i++){
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent updateServiceIntent = new Intent(context, NotificationReciever.class);
            PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(context, 2020+i, updateServiceIntent, 0);

            try {
                alarmManager.cancel(pendingUpdateIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
