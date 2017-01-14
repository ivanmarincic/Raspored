package com.idiotnation.raspored.Presenters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idiotnation.raspored.Contracts.MainContract;
import com.idiotnation.raspored.Modules.HTMLConverter;
import com.idiotnation.raspored.Modules.NotificationLoader;
import com.idiotnation.raspored.Objects.TableCell;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;
import com.idiotnation.raspored.Widget.RasporedWidgetProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.idiotnation.raspored.Utils.ERROR_INTERNAL;
import static com.idiotnation.raspored.Utils.ERROR_INTERNET;
import static com.idiotnation.raspored.Utils.ERROR_UNAVAILABLE;
import static com.idiotnation.raspored.Utils.INFO_FINISHED;
import static com.idiotnation.raspored.Utils.INFO_MESSAGE;

public class MainPresenter implements MainContract.Presenter {

    MainContract.View view;

    ;
    Context context;
    List<List<TableCell>> columns;

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
    public void download(String url) {
        try {
            if (url != "NN") {
                HTMLConverter htmlConverter = new HTMLConverter(context, url);
                htmlConverter.setFinishListener(new HTMLConverter.HTMLConverterListener() {
                    @Override
                    public void onFinish(List<List<TableCell>> columns) {
                        if (columns != null) {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    htmlConverter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    htmlConverter.execute();
                }
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
    public List<List<TableCell>> getRaspored() {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(context.getFilesDir(), "raspored.json")));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
        } finally {
            return (List<List<TableCell>>) new Gson().fromJson(text.toString(), new TypeToken<List<List<TableCell>>>() {
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
                download(url);
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
        NotificationLoader notificationLoader = new NotificationLoader(context, getRaspored());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            notificationLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            notificationLoader.execute();
        }
    }

    @Override
    public void populateHours(RelativeLayout parentView, Context context) {
        for (int i = 0; i < 13; i++) {
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            textView.setText(String.format("%02d", 7 + i) + ":00");
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextColor(Utils.getColor(R.color.hoursTextColorPrimary, context));
            float scale = context.getResources().getDisplayMetrics().density;
            GradientDrawable textViewBg = (GradientDrawable) context.getResources().getDrawable(R.drawable.separator).getConstantState().newDrawable();
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
        try {
            String stringDate = view.getPreferences().getString("UpdateTimeStamp", "default");
            Date currentDate;
            if (stringDate.equals("default")) {
                currentDate = new Date();
                view.getPreferences().edit().putString("UpdateTimeStamp", new Timestamp(new Date().getTime()).toString()).apply();
            } else {
                currentDate = new Date(Timestamp.valueOf(stringDate).getTime());
            }
            stringDate = new SimpleDateFormat("dd.MM.yyyy").format(currentDate);
            return new String("http://intranet.fsr.ba/intranetfsr/teamworks.dll/calendar/calendar" + getDegreeRasporedIndex(index) + "/calendar?StartDatee1=" + stringDate + "&DatePickerStartDatee1=&SelectDatee1=" + stringDate + "&DayCounte1=7&ViewTypee1=day&OQS=3-49&parenttagid=e1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void refreshWidget() {
        Intent updateIntent = new Intent(context, RasporedWidgetProvider.class);
        updateIntent.putExtra(Utils.WIDGET_INTENT, Utils.WIDGET_UPDATE);
        context.sendBroadcast(updateIntent);
    }

    @Override
    public int getPageNumber() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Sarajevo"));
        int day = convertDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
        if (day <= 4) {
            return day;
        }
        List<List<TableCell>> raspored = getRaspored();
        if (raspored != null) {
            mainloop:
            for (int i = raspored.size() - 1; i >= 0; i--) {
                for (int j = raspored.get(i).size() - 1; j >= 0; j--) {
                    if (raspored.get(i).get(j).getEnd().compareTo(new Date()) > 0 && day > 4) {
                        view.getPreferences().edit().putString("UpdateTimeStamp", new Timestamp(nextMonday(Calendar.MONDAY).getTimeInMillis()).toString()).apply();
                        return 0;
                    }
                    break mainloop;
                }
            }
        }
        return day;
    }

    private int convertDayOfWeek(int number) {
        switch (number) {
            case Calendar.MONDAY:
                return 0;
            case Calendar.TUESDAY:
                return 1;
            case Calendar.WEDNESDAY:
                return 2;
            case Calendar.THURSDAY:
                return 3;
            case Calendar.FRIDAY:
                return 4;
            case Calendar.SATURDAY:
                return 5;
            case Calendar.SUNDAY:
                return 6;
            default:
                return 0;
        }
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

    private Calendar nextMonday(int day) {
        Calendar date = Calendar.getInstance();
        int diff = day - date.get(Calendar.DAY_OF_WEEK);
        if (!(diff > 0)) {
            diff += 7;
        }
        date.add(Calendar.DAY_OF_MONTH, diff);
        return date;
    }

}
