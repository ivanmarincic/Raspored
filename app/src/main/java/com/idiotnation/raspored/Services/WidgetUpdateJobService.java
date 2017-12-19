package com.idiotnation.raspored.Services;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.view.View;
import android.widget.RemoteViews;

import com.idiotnation.raspored.Models.LessonCell;
import com.idiotnation.raspored.Models.WidgetData;
import com.idiotnation.raspored.Presenters.MainPresenter;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;
import com.idiotnation.raspored.Widget.RasporedWidgetProvider;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

public class WidgetUpdateJobService extends JobIntentService {

    MainPresenter presenter;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, WidgetUpdateJobService.class, Utils.WIDGET_UPDATE_UNIQUE_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        presenter = new MainPresenter();
        presenter.start(null, getApplicationContext());

        try {
            WidgetData widgetData = getAppropriateItem(presenter.getRaspored());
            LessonCell lessonCell = widgetData.lessonCell;
            DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

            if (lessonCell != null && allWidgetIds != null) {
                for (int widgetId : allWidgetIds) {
                    RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.raspored_widget_layout);
                    remoteViews.setTextViewText(R.id.widget_text_content, lessonCell.getText());
                    remoteViews.setTextColor(R.id.widget_text_content, Utils.getColor(R.color.widgetTextColorPrimary, getApplicationContext()));
                    if (widgetData.numberOfLessons > 0) {
                        if (lessonCell.getStart() != null || lessonCell.getEnd() != null) {
                            remoteViews.setTextViewText(R.id.widget_text_time, getDayOfWeek(lessonCell.getStart()) + "  " + timeFormatter.print(lessonCell.getStart()) + " - " + timeFormatter.print(lessonCell.getEnd()));
                            remoteViews.setTextColor(R.id.widget_text_time, Utils.getColor(R.color.widgetTextColorPrimary, getApplicationContext()));
                        } else {
                            remoteViews.setTextViewText(R.id.widget_text_time, "");
                            remoteViews.setViewVisibility(R.id.widget_text_time, View.GONE);
                        }
                        remoteViews.setTextViewText(R.id.widget_text_exams, "Ispiti: " + widgetData.numberOfExams + "/" + widgetData.numberOfLessons);
                        remoteViews.setTextColor(R.id.widget_text_exams, Utils.getColor(R.color.widgetTextColorPrimary, getApplicationContext()));
                    } else {
                        remoteViews.setViewVisibility(R.id.widget_text_exams, View.GONE);
                        remoteViews.setViewVisibility(R.id.widget_text_time, View.GONE);
                        remoteViews.setViewPadding(R.id.widget_text_content, 0, 20, 0, 20);
                    }
                    remoteViews.setInt(R.id.widget_text_container, "setBackgroundColor", Utils.getColor(R.color.widgetBackgroundColor, getApplicationContext()));
                    Intent clickIntent = new Intent(getApplicationContext(), RasporedWidgetProvider.class);
                    clickIntent.putExtra(Utils.WIDGET_INTENT, Utils.WIDGET_CLICK);
                    PendingIntent clickPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), Utils.WIDGET_CLICK_INTENT, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.widget_text_container, clickPendingIntent);
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopSelf();
    }

    private WidgetData getAppropriateItem(List<List<LessonCell>> columns) {
        WidgetData widgetData = new WidgetData();
        if (columns != null) {
            List<LessonCell> lessons = Utils.shrinkList(columns);
            widgetData.numberOfExams = 0;
            widgetData.numberOfLessons = lessons.size();
            boolean isCellFound = false;
            for (int i = 0; i < lessons.size(); i++) {
                LessonCell lessonCell = lessons.get(i);
                if (lessonCell.getText().contains("kolokvij") || lessonCell.getText().contains("ispit")) {
                    widgetData.numberOfExams++;
                }
                if (lessonCell.getStart().isAfterNow() && !isCellFound) {
                    widgetData.lessonCell = lessonCell;
                    isCellFound = true;
                }
            }
            if (widgetData.lessonCell == null) {
                widgetData.lessonCell = new LessonCell();
                widgetData.lessonCell.setText(getResources().getString(R.string.info_empty_widget));
            }
        } else {
            widgetData.lessonCell = new LessonCell();
            widgetData.lessonCell.setText(getResources().getString(R.string.error_msg_widget));
        }
        return widgetData;
    }

    private String getDayOfWeek(DateTime date) {
        switch (date.getDayOfWeek()) {
            case 7:
                return "Nedjelja";
            case 1:
                return "Ponedjeljak";
            case 2:
                return "Utorak";
            case 3:
                return "Srijeda";
            case 4:
                return "Četvrtak";
            case 5:
                return "Petak";
            case 6:
                return "Subota";
            default:
                return "";
        }
    }
}
