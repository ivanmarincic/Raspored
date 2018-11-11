package com.idiotnation.raspored;

import android.os.Build;

import com.evernote.android.job.JobManager;
import com.idiotnation.raspored.dataaccess.database.DatabaseSqliteOpenHelper;
import com.idiotnation.raspored.helpers.JobCreator;
import com.idiotnation.raspored.helpers.NotificationChannels;

import net.danlew.android.joda.JodaTimeAndroid;

import androidx.multidex.MultiDexApplication;

public class Application extends MultiDexApplication {

    private static DatabaseSqliteOpenHelper helper;

    @Override
    public void onCreate() {
        super.onCreate();
        helper = new DatabaseSqliteOpenHelper(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannels.createAppointmentsNotificationChannel(getApplicationContext());
            NotificationChannels.createChangesNotificationChannel(getApplicationContext());
        }
        JodaTimeAndroid.init(this);
        JobManager.create(this).addJobCreator(new JobCreator());
    }

    public static DatabaseSqliteOpenHelper getDatabaseHelper() {
        return helper;
    }
}
