package com.idiotnation.raspored.helpers;

import android.content.Context;

import com.idiotnation.raspored.BuildConfig;
import com.idiotnation.raspored.R;

import org.joda.time.DateTimeConstants;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static final String AUTO_UPDATE_JOB_TAG = "com.idiotnation.Raspored.AutoUpdateJobService";
    public static final String AUTO_UPDATE_JOB_ID = "AUTO_UPDATE_JOB_ID";

    public static final String APPOINTEMENT_NOTIFICATIONS_JOB_TAG = "com.idiotnation.Raspored.AppointmentNotificationsJobService";
    public static final String APPOINTEMENT_NOTIFICATIONS_JOB_ID = "APPOINTEMENT_NOTIFICATIONS_JOB_ID";

    public static final String NOTIFICATION_CHANNEL_CHANGES_ID = "com.idiotnation.Raspored.Changes";
    public static final String NOTIFICATION_CHANNEL_APPOINTMENTS_ID = "com.idiotnation.Raspored.Changes";

    public static final Integer NOTIFICATION_CHAGNES_ID = 7007148;
    public static final Integer NOTIFICATION_APPOINTMENTS_ID = 728585;

    public static final String WS_BASE_URL = BuildConfig.WS_BASE_URL;

    public static final Integer SETTINGS_RESULT_CODE = 241;

    public static final String SETTINGS_RESULT_EXTRAS = "SETTINGS_EXTRAS";
    public static final Integer SETTINGS_RESULT_EXTRAS_UPDATE = 1;
    public static final Integer SETTINGS_RESULT_EXTRAS_JOB = 2;
    public static final Integer SETTINGS_RESULT_EXTRAS_NOTIFICATIONS = 4;

    public static final int PERMISSIONS_READ_WRITE_CALENDAR = 555;


    public static <P, D> List<P> convertToPojo(List<D> dtos, Class<P> pojoClass) {
        List<P> result = new ArrayList<>();
        try {
            for (D dto : dtos) {
                Method toPojo = dto.getClass().getMethod("toPojo");
                result.add(pojoClass.cast(toPojo.invoke(dto)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static <P, D> List<D> convertToDto(List<P> pojos, Class<D> dtoClass) {
        List<D> result = new ArrayList<>();
        try {
            for (P pojo : pojos) {
                Constructor constructor = dtoClass.getConstructor(pojo.getClass());
                result.add((D) constructor.newInstance(pojo));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getDayOfWeekString(int dayOfWeek, Context context) {
        switch (dayOfWeek) {
            case DateTimeConstants.MONDAY:
                return context.getResources().getString(R.string.day_of_week_monday);
            case DateTimeConstants.TUESDAY:
                return context.getResources().getString(R.string.day_of_week_tuesday);
            case DateTimeConstants.WEDNESDAY:
                return context.getResources().getString(R.string.day_of_week_wednesday);
            case DateTimeConstants.THURSDAY:
                return context.getResources().getString(R.string.day_of_week_thursday);
            case DateTimeConstants.FRIDAY:
                return context.getResources().getString(R.string.day_of_week_friday);
            case DateTimeConstants.SATURDAY:
                return context.getResources().getString(R.string.day_of_week_saturday);
            case DateTimeConstants.SUNDAY:
                return context.getResources().getString(R.string.day_of_week_sunday);
            default:
                return "";
        }
    }

    public static String listToString(List list) {
        StringBuilder buf = new StringBuilder();
        if (list.size() > 0) {
            String sep = ", ";
            buf.append(list.get(0));
            for (int i = 1; i < list.size(); i++) {
                buf.append(sep).append(list.get(i).toString());
            }
        }
        return buf.toString();
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        } else if (o1 == null || o2 == null) {
            return false;
        } else return o1.equals(o2);
    }

    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }
}
