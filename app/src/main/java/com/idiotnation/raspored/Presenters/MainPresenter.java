package com.idiotnation.raspored.Presenters;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.idiotnation.raspored.Contracts.MainContract;
import com.idiotnation.raspored.Helpers.BackgroundTask;
import com.idiotnation.raspored.Models.LessonCell;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Tasks.DownloadTask;
import com.idiotnation.raspored.Tasks.NotificationLoaderTask;
import com.idiotnation.raspored.Utils;
import com.idiotnation.raspored.Widget.RasporedWidgetProvider;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.idiotnation.raspored.Recievers.NotificationReceiver.NOTIFICATION_CHANNEL_ID;
import static com.idiotnation.raspored.Utils.ERROR_INTERNAL;
import static com.idiotnation.raspored.Utils.ERROR_INTERNET;
import static com.idiotnation.raspored.Utils.ERROR_UNAVAILABLE;
import static com.idiotnation.raspored.Utils.INFO_FINISHED;
import static com.idiotnation.raspored.Utils.INFO_MESSAGE;

public class MainPresenter implements MainContract.Presenter {

    MainContract.View view;

    Context context;

    public MainPresenter() {
    }

    @Override
    public void start(MainContract.View view, Context context) {
        this.view = view;
        this.context = context;
        if (view != null) {
            view.initialize();
        }
    }

    @Override
    public void download(String url, int index) {
        try {
            if (!url.equals("NN")) {
                DownloadTask downloadTask = new DownloadTask(context, url, getDegreeRasporedIndex(index));
                downloadTask.setFinishListener(new BackgroundTask.onFinishListener() {
                    @Override
                    public <Type> void onFinish(Type t) {
                        if (t != null && ((List<List<LessonCell>>) t).size() > 0) {
                            view.showMessage(View.VISIBLE, INFO_FINISHED);
                            refreshNotifications();
                            view.setRaspored(getRaspored());
                            refreshWidget();
                        } else {
                            view.stopAnimation();
                            view.showMessage(View.VISIBLE, ERROR_INTERNAL);
                        }
                    }
                });
                downloadTask.run();
            } else {
                view.showMessage(View.VISIBLE, ERROR_UNAVAILABLE);
                view.stopAnimation();
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.showMessage(View.VISIBLE, ERROR_INTERNAL);
            view.stopAnimation();
        }
    }

    @Override
    public List<List<LessonCell>> getRaspored() {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(context.getFilesDir(), "raspored.json")));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException ignored) {
        } finally {
            Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
            return (List<List<LessonCell>>) gson.fromJson(text.toString(), new TypeToken<List<List<LessonCell>>>() {
            }.getType());
        }
    }

    @Override
    public void refresh(final int idNumber) {
        if (idNumber != -1) {
            view.startAnimation();
            String url = getRasporedUrl(idNumber);
            if (url != null) {
                view.getPreferences().edit().putInt("SpinnerDefault", idNumber).apply();
                download(url, idNumber);
            } else {
                view.stopAnimation();
                view.showMessage(View.VISIBLE, ERROR_INTERNET);
            }
        } else {
            view.showMessage(View.VISIBLE, INFO_MESSAGE);
            view.stopAnimation();
        }
    }

    @Override
    public void refreshNotifications() {
        NotificationLoaderTask notificationLoader = new NotificationLoaderTask(context, getRaspored());
        notificationLoader.run();
    }

    @Override
    public void populateHours(RelativeLayout parentView, Context context) {
        for (int i = 0; i < 13; i++) {
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            textView.setText(String.format("%s:00", String.format(Locale.ENGLISH, "%02d", 7 + i)));
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextColor(Utils.getColor(R.color.hoursTextColorPrimary, context));
            float scale = context.getResources().getDisplayMetrics().density;
            GradientDrawable textViewBg = new GradientDrawable();
            textViewBg.setShape(GradientDrawable.RECTANGLE);
            textViewBg.setStroke((int) (1 * scale + 0.5f), Utils.getColor(R.color.hoursBackgroundStrokeColor, context));
            textViewBg.setColor(Utils.getColor(R.color.hoursBackgroundColor, context));
            textView.setBackgroundDrawable(textViewBg);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, i == 12 ? ViewGroup.LayoutParams.MATCH_PARENT : parentView.getHeight() / 13);
            params.topMargin = (parentView.getHeight() / 13) * i;
            textView.setLayoutParams(params);
            parentView.addView(textView);
        }
    }

    @Override
    public String getRasporedUrl(int index) {
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("dd.MM.yyyy");
        DateTime updateTime;
        List<LessonCell> raspored = Utils.shrinkList(getRaspored());
        if (new DateTime().getDayOfWeek() > 6) {
            updateTime = Utils.nextMonday();
        } else {
            updateTime = Utils.thisMonday();
        }
        view.getPreferences().edit().putString("UpdateTime", updateTime.toString()).apply();
        return "http://intranet.fsr.ba/intranetfsr/teamworks.dll/calendar/calendar" + getDegreeRasporedIndex(index) + "/calendar?" + "StartDatee1=" + dtfOut.print(updateTime);
    }

    @Override
    public void refreshWidget() {
        Intent updateIntent = new Intent(context, RasporedWidgetProvider.class);
        updateIntent.putExtra(Utils.WIDGET_INTENT, Utils.WIDGET_UPDATE);
        context.sendBroadcast(updateIntent);
    }

    @Override
    public int getPageNumber() {
        DateTime dateTime = new DateTime();
        int day = dateTime.getDayOfWeek() - 1;
        if (dateTime.getHourOfDay() >= 19) {
            day++;
        }
        if (day >= 6) {
            day = 5;
        }
        if (getLastUpdateTime().isAfterNow() && day >= 4) {
            day = 0;
        }
        return day;
    }

    @Override
    public void initNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                context.getResources().getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getResources().getString(R.string.notification_channel_description));
        notificationManager.createNotificationChannel(channel);
    }

    private int getDegreeRasporedIndex(int index) {
        switch (index) {
            case 0:
                return 6;
            case 1:
                return 16;
            case 2:
                return 17;
            case 3:
                return 18;
            case 4:
                return 20;
            case 5:
                return 21;
            case 6:
                return 22;
            case 7:
                return 24;
            case 8:
                return 25;
            case 9:
                return 26;
            case 10:
                return 28;
            case 11:
                return 1;
            case 12:
                return 2;
            case 13:
                return 3;
            case 14:
                return 7;
            case 15:
                return 4;
            case 16:
                return 8;
            case 17:
                return 5;
            case 18:
                return 44;
        }
        return 0;
    }

    public DateTime getLastUpdateTime() {
        try {
            return new DateTime(view.getPreferences().getString("UpdateTime", Utils.thisMonday().toString()));
        } catch (Exception e) {
            return Utils.thisMonday();
        }
    }

}
