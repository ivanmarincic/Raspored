package com.idiotnation.raspored.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.idiotnation.raspored.R;

import androidx.annotation.RequiresApi;

public class NotificationChannels {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createChangesNotificationChannel(Context context) {
        CharSequence name = context.getResources().getString(R.string.notification_channel_changes_name);
        String description = context.getResources().getString(R.string.notification_channel_changes_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(Utils.NOTIFICATION_CHANNEL_CHANGES_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createAppointmentsNotificationChannel(Context context) {
        CharSequence name = context.getResources().getString(R.string.notification_channel_appointments_name);
        String description = context.getResources().getString(R.string.notification_channel_appointments_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(Utils.NOTIFICATION_CHANNEL_APPOINTMENTS_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
