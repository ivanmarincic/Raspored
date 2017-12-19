package com.idiotnation.raspored.Recievers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.idiotnation.raspored.R;

import static android.support.v4.app.NotificationCompat.DEFAULT_LIGHTS;
import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_ID = "RasporedNotification";
    public static final String NOTIFICATION = "RasporedNotificationContent";
    public static final String NOTIFICATION_CHANNEL_ID = "com.idiotnation.raspored";

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = getLessonNotification(intent.getStringExtra(NOTIFICATION), context);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(id, notification);
    }

    private Notification getLessonNotification(String text, Context context){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE | DEFAULT_LIGHTS)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Predavanje")
                        .setContentText(text + " za pola sata");
        return mBuilder.build();
    }

}
