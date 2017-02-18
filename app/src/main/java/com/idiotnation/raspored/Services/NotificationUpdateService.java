package com.idiotnation.raspored.Services;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.idiotnation.raspored.Presenters.MainPresenter;
import com.idiotnation.raspored.Tasks.NotificationLoaderTask;

public class NotificationUpdateService extends Service {

    MainPresenter presenter;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        presenter = new MainPresenter();
        presenter.start(null, getApplicationContext());

        try {
            NotificationLoaderTask notificationLoader = new NotificationLoaderTask(getApplicationContext(), presenter.getRaspored());
            notificationLoader.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopSelf();
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
