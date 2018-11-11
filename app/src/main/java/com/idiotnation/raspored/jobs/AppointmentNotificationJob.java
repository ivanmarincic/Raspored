package com.idiotnation.raspored.jobs;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.views.MainView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AppointmentNotificationJob extends Job {

    public static final String NOTIFICATION_TITLE = "NOTIFICATION_TITLE";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        String title = params.getExtras().getString(NOTIFICATION_TITLE, null);
        if (title != null) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
            notificationManager.notify(Utils.NOTIFICATION_APPOINTMENTS_ID, createNotification(title));
        }
        return Result.SUCCESS;
    }

    public static int scheduleJob(long miliseconds) {
        return new JobRequest.Builder(Utils.APPOINTEMENT_NOTIFICATIONS_JOB_TAG)
                .setExact(miliseconds)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .setRequirementsEnforced(true)
                .setUpdateCurrent(false)
                .build()
                .schedule();
    }

    private Notification createNotification(String title) {
        Intent intent = new Intent(getContext(), MainView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat
                .Builder(getContext(), Utils.NOTIFICATION_CHANNEL_APPOINTMENTS_ID)
                .setSmallIcon(R.drawable.ic_statusbar_notification)
                .setContentTitle(title)
                .setContentText(getContext().getResources().getString(R.string.notification_changes_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }
}
