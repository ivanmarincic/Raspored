package com.idiotnation.raspored;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.idiotnation.raspored.Utils.getGDiskId;

public class AutoUpdateService extends Service {

    SharedPreferences prefs;
    List<String> rasporedUrls;
    AlarmManager alarm;
    String rasporedPath = "";
    int updateDelay = 1; // in hours

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prefs = getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
        rasporedUrls = new ArrayList<>();
        alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        rasporedPath = getFilesDir().getAbsolutePath() + "/raspored.pdf";
        if (prefs.getBoolean("AutoUpdate", false)) {
            try {
                DegreeLoader degreeLoader = new DegreeLoader(getApplicationContext());
                degreeLoader.setOnFinishListener(new DegreeLoader.onFinihListener() {
                    @Override
                    public void onFinish(List list) {
                        if (list != null) {
                            rasporedUrls = list;
                            if (rasporedUrls.size() >= 10) {
                                try {
                                    if (checkForUpdate(getGDiskId(rasporedUrls.get(prefs.getInt("SpinnerDefault", 0))))) {
                                        Intent notificationIntent = new Intent(getApplicationContext(), MainView.class);
                                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        PendingIntent notificationPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
                                        NotificationCompat.Builder mBuilder =
                                                new NotificationCompat.Builder(getApplicationContext())
                                                        .setSmallIcon(R.drawable.update)
                                                        .setContentTitle("Raspored")
                                                        .setContentText("Dostupan novi raspored")
                                                        .setAutoCancel(true)
                                                        .setContentIntent(notificationPendingIntent);
                                        NotificationManager mNotifyMgr =
                                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                        mNotifyMgr.notify(29101996, mBuilder.build());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
                degreeLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stopSelf();
            }
        }
        return Service.START_NOT_STICKY;
    }

    public boolean checkForUpdate(String id) {
        if (prefs.getString("CurrentRasporedId", "NN") != "NN") {
            if (prefs.getString("CurrentRasporedId", "NN").equals(id) && new File(rasporedPath).exists()) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public static boolean isReachableByTcp(String host, int port, int timeout) {
        try {
            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress, timeout);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        if (prefs.getBoolean("AutoUpdate", false)) {
            Intent intent = new Intent(AutoUpdateService.this, AutoUpdateService.class);
            PendingIntent pendingIntent = PendingIntent.getService(AutoUpdateService.this, 0, intent, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.HOUR_OF_DAY, updateDelay); // first time
            alarm.set(alarm.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
