package com.idiotnation.raspored.jobs;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.dto.AppointmentFilterDto;
import com.idiotnation.raspored.models.dto.CalendarFilterDto;
import com.idiotnation.raspored.models.dto.SettingsItemDto;
import com.idiotnation.raspored.services.AppointmentService;
import com.idiotnation.raspored.views.MainView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

public class AutoUpdateJob extends Job {

    private Notification notification;
    private Disposable disposable;

    @Override
    @NonNull
    protected Result onRunJob(@NonNull Params params) {
        final SharedPreferences sharedPreferences = getContext().getSharedPreferences(getContext().getPackageName(), MODE_PRIVATE);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        new AppointmentService()
                .autoUpdateSync(getAppointmentFilter(sharedPreferences), getCalendarFilter(sharedPreferences), getContext())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(Boolean hasBeenUpdated) {
                        if (hasBeenUpdated) {
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                            notificationManager.notify(Utils.NOTIFICATION_CHAGNES_ID, createNotification());
                            sharedPreferences
                                    .edit()
                                    .putString(SettingsItemDto.SETTINGS_TYPE_LAST_SYNC, DateTime.now().withZone(DateTimeZone.UTC).toString())
                                    .apply();
                        }
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        countDownLatch.countDown();
                    }
                });
        try {
            countDownLatch.await();
        } catch (InterruptedException ignored) {
        }
        return Result.SUCCESS;
    }

    private AppointmentFilterDto getAppointmentFilter(SharedPreferences sharedPreferences) {
        AppointmentFilterDto appointmentFilterDto = new AppointmentFilterDto();
        String lastSyncString = sharedPreferences.getString(SettingsItemDto.SETTINGS_TYPE_LAST_SYNC, null);
        if (lastSyncString != null) {
            appointmentFilterDto.setLastSync(DateTime.parse(lastSyncString));
        }
        appointmentFilterDto.setCourseId(sharedPreferences.getInt(SettingsItemDto.SETTINGS_TYPE_COURSE, -1));
        appointmentFilterDto.setPartialCourseId(sharedPreferences.getInt(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE, -1));
        appointmentFilterDto.setPartialStrings(new ArrayList<>(sharedPreferences.getStringSet(SettingsItemDto.SETTINGS_TYPE_PARTIAL, new HashSet<String>())));
        appointmentFilterDto.setBlockedStrings(new ArrayList<>(sharedPreferences.getStringSet(SettingsItemDto.SETTINGS_TYPE_BLOCKED, new HashSet<String>())));
        return appointmentFilterDto;
    }

    private CalendarFilterDto getCalendarFilter(SharedPreferences sharedPreferences) {
        CalendarFilterDto calendarFilterDto = new CalendarFilterDto();
        calendarFilterDto.setCalendarId(sharedPreferences.getInt(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC_ID, -1));
        calendarFilterDto.setSyncId(sharedPreferences.getString(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC_UUID, null));
        return calendarFilterDto;
    }

    @Override
    protected void onCancel() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public static int scheduleJob() {
        return new JobRequest.Builder(Utils.AUTO_UPDATE_JOB_TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(30), TimeUnit.MINUTES.toMillis(5))
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.NOT_ROAMING)
                .setRequirementsEnforced(true)
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }

    private Notification createNotification() {
        if (notification == null) {
            Intent intent = new Intent(getContext(), MainView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat
                    .Builder(getContext(), Utils.NOTIFICATION_CHANNEL_CHANGES_ID)
                    .setSmallIcon(R.drawable.ic_statusbar_notification)
                    .setContentTitle(getContext().getResources().getString(R.string.notification_changes_title))
                    .setContentText(getContext().getResources().getString(R.string.notification_changes_text))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            notification = notificationBuilder.build();
        }
        return notification;
    }
}
