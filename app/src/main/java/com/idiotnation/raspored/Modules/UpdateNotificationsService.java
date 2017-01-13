package com.idiotnation.raspored.Modules;


import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.idiotnation.raspored.Presenters.MainPresenter;

public class UpdateNotificationsService extends Service {

    MainPresenter presenter;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        presenter = new MainPresenter();
        presenter.start(null, getApplicationContext());

        try {
            NotificationLoader notificationLoader = new NotificationLoader(getApplicationContext(), presenter.getRaspored());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                notificationLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                notificationLoader.execute();
            }
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
