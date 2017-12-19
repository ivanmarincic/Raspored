package com.idiotnation.raspored.Services;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.idiotnation.raspored.Presenters.MainPresenter;
import com.idiotnation.raspored.Tasks.NotificationLoaderTask;
import com.idiotnation.raspored.Utils;

public class NotificationUpdateJobService extends JobIntentService {

    MainPresenter presenter;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, NotificationUpdateJobService.class, Utils.NOTIFICATION_UPDATE_UNIQUE_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        presenter = new MainPresenter();
        presenter.start(null, getApplicationContext());

        try {
            NotificationLoaderTask notificationLoader = new NotificationLoaderTask(getApplicationContext(), presenter.getRaspored());
            notificationLoader.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopSelf();
    }

}
