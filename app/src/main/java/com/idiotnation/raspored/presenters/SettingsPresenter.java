package com.idiotnation.raspored.presenters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.widget.Toast;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.contracts.SettingsContract;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.dto.CourseFilterDto;
import com.idiotnation.raspored.models.dto.SettingsItemDto;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class SettingsPresenter implements SettingsContract.Presenter {

    private SettingsContract.View view;
    private Context context;
    private SharedPreferences sharedPreferences;
    private HashMap<String, SettingsItemDto> currentSettings;

    @Override
    public void start(SettingsContract.View view, Context context) {
        this.view = view;
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        view.initialize();
    }

    @Override
    public void getSettings() {
        Single
                .fromCallable(new Callable<HashMap<String, SettingsItemDto>>() {
                    @Override
                    public HashMap<String, SettingsItemDto> call() {
                        HashMap<String, SettingsItemDto> settings = new HashMap<>();
                        settings.put(SettingsItemDto.SETTINGS_TYPE_COURSE, new SettingsItemDto(sharedPreferences.getInt(SettingsItemDto.SETTINGS_TYPE_COURSE, -1), SettingsItemDto.SETTINGS_TYPE_COURSE));
                        settings.put(SettingsItemDto.SETTINGS_TYPE_COURSE_NAME, new SettingsItemDto(sharedPreferences.getString(SettingsItemDto.SETTINGS_TYPE_COURSE_NAME, null), SettingsItemDto.SETTINGS_TYPE_COURSE_NAME));
                        settings.put(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE, new SettingsItemDto(sharedPreferences.getInt(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE, -1), SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE));
                        settings.put(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE_NAME, new SettingsItemDto(sharedPreferences.getString(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE_NAME, null), SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE_NAME));
                        List<String> partialStrings = new ArrayList<>(sharedPreferences.getStringSet(SettingsItemDto.SETTINGS_TYPE_PARTIAL, new HashSet<String>()));
                        settings.put(SettingsItemDto.SETTINGS_TYPE_PARTIAL, new SettingsItemDto(partialStrings, SettingsItemDto.SETTINGS_TYPE_PARTIAL));
                        List<String> blockedStrings = new ArrayList<>(sharedPreferences.getStringSet(SettingsItemDto.SETTINGS_TYPE_BLOCKED, new HashSet<String>()));
                        settings.put(SettingsItemDto.SETTINGS_TYPE_BLOCKED, new SettingsItemDto(blockedStrings, SettingsItemDto.SETTINGS_TYPE_BLOCKED));
                        Boolean notificationsToggled = sharedPreferences.getBoolean(SettingsItemDto.SETTINGS_TYPE_NOTIFICATIONS, false);
                        settings.put(SettingsItemDto.SETTINGS_TYPE_NOTIFICATIONS, new SettingsItemDto(notificationsToggled, SettingsItemDto.SETTINGS_TYPE_NOTIFICATIONS));
                        Boolean autoSyncToggled = sharedPreferences.getBoolean(SettingsItemDto.SETTINGS_TYPE_AUTOSYNC, false);
                        settings.put(SettingsItemDto.SETTINGS_TYPE_AUTOSYNC, new SettingsItemDto(autoSyncToggled, SettingsItemDto.SETTINGS_TYPE_AUTOSYNC));
                        Boolean calendarSyncToggled = sharedPreferences.getBoolean(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC, false);
                        settings.put(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC, new SettingsItemDto(calendarSyncToggled, SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC));
                        String courseLastSyncString = sharedPreferences.getString(SettingsItemDto.SETTINGS_TYPE_COURSE_LAST_SYNC, null);
                        if (courseLastSyncString != null && settings.get(SettingsItemDto.SETTINGS_TYPE_COURSE).getValue() != null) {
                            settings.put(SettingsItemDto.SETTINGS_TYPE_COURSE_LAST_SYNC, new SettingsItemDto(DateTime.parse(courseLastSyncString), SettingsItemDto.SETTINGS_TYPE_COURSE_LAST_SYNC));
                        } else {
                            settings.put(SettingsItemDto.SETTINGS_TYPE_COURSE_LAST_SYNC, new SettingsItemDto(null, SettingsItemDto.SETTINGS_TYPE_COURSE_LAST_SYNC));
                        }
                        String appointmentsLastSyncString = sharedPreferences.getString(SettingsItemDto.SETTINGS_TYPE_LAST_SYNC, null);
                        if (appointmentsLastSyncString != null) {
                            settings.put(SettingsItemDto.SETTINGS_TYPE_LAST_SYNC, new SettingsItemDto(DateTime.parse(appointmentsLastSyncString), SettingsItemDto.SETTINGS_TYPE_LAST_SYNC));
                        } else {
                            settings.put(SettingsItemDto.SETTINGS_TYPE_LAST_SYNC, new SettingsItemDto(null, SettingsItemDto.SETTINGS_TYPE_LAST_SYNC));
                        }
                        currentSettings = settings;
                        return settings;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<HashMap<String, SettingsItemDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(HashMap<String, SettingsItemDto> settings) {
                        view.loadSettings(settings);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void saveSettings(HashMap<String, SettingsItemDto> settings) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SettingsItemDto.SETTINGS_TYPE_COURSE, settings.get(SettingsItemDto.SETTINGS_TYPE_COURSE).getValue(Integer.class));
        Object courseName = settings.get(SettingsItemDto.SETTINGS_TYPE_COURSE_NAME).getValue();
        if (courseName != null) {
            editor.putString(SettingsItemDto.SETTINGS_TYPE_COURSE_NAME, courseName.toString());
        }
        editor.putInt(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE, settings.get(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE).getValue(Integer.class));
        Object partialCourseName = settings.get(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE_NAME).getValue();
        if (partialCourseName != null) {
            editor.putString(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE_NAME, partialCourseName.toString());
        }
        editor.putStringSet(SettingsItemDto.SETTINGS_TYPE_PARTIAL, new HashSet<String>(settings.get(SettingsItemDto.SETTINGS_TYPE_PARTIAL).getValue(List.class)));
        editor.putStringSet(SettingsItemDto.SETTINGS_TYPE_BLOCKED, new HashSet<String>(settings.get(SettingsItemDto.SETTINGS_TYPE_BLOCKED).getValue(List.class)));
        editor.putBoolean(SettingsItemDto.SETTINGS_TYPE_NOTIFICATIONS, settings.get(SettingsItemDto.SETTINGS_TYPE_NOTIFICATIONS).getValue(Boolean.class));
        editor.putBoolean(SettingsItemDto.SETTINGS_TYPE_AUTOSYNC, settings.get(SettingsItemDto.SETTINGS_TYPE_AUTOSYNC).getValue(Boolean.class));
        Boolean calendarSyncToggled = settings.get(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC).getValue(Boolean.class);
        editor.putBoolean(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC, calendarSyncToggled);
        if (!calendarSyncToggled) {
            editor.remove(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC_ID);
        }
        Object courseLastSync = settings.get(SettingsItemDto.SETTINGS_TYPE_COURSE_LAST_SYNC).getValue();
        if (courseLastSync != null) {
            editor.putString(SettingsItemDto.SETTINGS_TYPE_COURSE_LAST_SYNC, courseLastSync.toString());
        } else {
            editor.remove(SettingsItemDto.SETTINGS_TYPE_COURSE_LAST_SYNC);
        }
        Object appointmentsLastSync = settings.get(SettingsItemDto.SETTINGS_TYPE_LAST_SYNC).getValue();
        if (appointmentsLastSync != null) {
            editor.putString(SettingsItemDto.SETTINGS_TYPE_LAST_SYNC, appointmentsLastSync.toString());
        } else {
            editor.remove(SettingsItemDto.SETTINGS_TYPE_LAST_SYNC);
        }
        editor.apply();
    }

    @Override
    public CourseFilterDto getCoursesFilter() {
        CourseFilterDto courseFilterDto = new CourseFilterDto();
        Object lastSync = currentSettings.get(SettingsItemDto.SETTINGS_TYPE_COURSE_LAST_SYNC).getValue();
        if (lastSync != null) {
            courseFilterDto.setLastSync((DateTime) lastSync);
        }
        return courseFilterDto;
    }

    @Override
    @SuppressLint("MissingPermission")
    public boolean getCalendarId() {
        String[] permissions = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};
        if (EasyPermissions.hasPermissions(context, permissions)) {
            String[] EVENT_PROJECTION = new String[]{
                    CalendarContract.Calendars._ID,                           // 0
            };
            int PROJECTION_ID_INDEX = 0;

            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = CalendarContract.Calendars.CONTENT_URI;
            String selection =
                    "((" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                            + CalendarContract.Calendars.IS_PRIMARY + " = ?))";
            String[] selectionArgs = new String[]{"com.google", "1"};
            Cursor cursor = contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (sharedPreferences.getString(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC_UUID, null) == null) {
                    editor.putString(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC_UUID, Utils.CALENDAR_SYNC_URI);
                }
                editor.putInt(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC_ID, cursor.getInt(PROJECTION_ID_INDEX));
                editor.apply();
                cursor.close();
                return true;
            } else {
                String selectionOther =
                        "("
                                + CalendarContract.Calendars.IS_PRIMARY + " = ?)";
                String[] selectionOtherArgs = new String[]{"1"};
                Cursor cursorOther = contentResolver.query(uri, EVENT_PROJECTION, selectionOther, selectionOtherArgs, null);
                if (cursorOther != null && cursorOther.moveToFirst()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (sharedPreferences.getString(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC_UUID, null) == null) {
                        editor.putString(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC_UUID, Utils.CALENDAR_SYNC_URI);
                    }
                    editor.putInt(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC_ID, cursorOther.getInt(PROJECTION_ID_INDEX));
                    editor.apply();
                    cursorOther.close();
                    return true;
                }
                Toast.makeText(context, context.getString(R.string.settings_view_list_value_calendar_sync_not_found), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            EasyPermissions.requestPermissions(new PermissionRequest
                    .Builder(view.getActivity(), Utils.PERMISSIONS_READ_WRITE_CALENDAR, permissions)
                    .setRationale(R.string.request_permission_rationale)
                    .setNegativeButtonText(R.string.request_permission_rationale_negative)
                    .setPositiveButtonText(R.string.request_permission_rationale_positive)
                    .build()
            );
        }
        return true;
    }
}
