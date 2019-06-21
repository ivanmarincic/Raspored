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

import com.evernote.android.job.JobManager;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.contracts.SettingsContract;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.jobs.AutoUpdateJob;
import com.idiotnation.raspored.models.dto.CourseDto;
import com.idiotnation.raspored.models.dto.CourseFilterDto;
import com.idiotnation.raspored.models.dto.FilteredCourseDto;
import com.idiotnation.raspored.models.dto.PartialCourseDto;
import com.idiotnation.raspored.models.dto.SettingsDto;
import com.idiotnation.raspored.services.AppointmentService;
import com.idiotnation.raspored.services.SettingsService;

import org.joda.time.DateTime;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class SettingsPresenter implements SettingsContract.Presenter {

    private SettingsContract.View view;
    private Context context;
    private SharedPreferences sharedPreferences;
    private AppointmentService appointmentService;
    private SettingsService settingsService;
    SettingsDto currentSettings;
    List<PartialCourseDto> currentPartials;
    List<FilteredCourseDto> currentFiltered;

    @Override
    public void start(SettingsContract.View view, Context context) {
        this.view = view;
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        appointmentService = new AppointmentService();
        settingsService = new SettingsService();
        if (view != null) {
            view.initialize();
        }
    }

    @Override
    public SettingsDto getSettings() {
        return currentSettings;
    }

    @Override
    public List<PartialCourseDto> getPartials() {
        return currentPartials;
    }

    @Override
    public List<FilteredCourseDto> getFiltered() {
        return currentFiltered;
    }

    @Override
    public void loadSettings() {
        currentSettings = settingsService.getSettings();
        currentPartials = settingsService.getPartials(currentSettings.getPartialCourse());
        currentFiltered = settingsService.getFiltered();
        view.loadSettings(currentSettings);
        view.loadPartials(currentSettings, currentPartials);
        view.loadFiltered(currentSettings, currentFiltered);
        view.hideLoading();
    }

    @Override
    public void setSyncNotifications(Boolean syncNotifications) {
        if (!settingsService.setSyncNotifications(syncNotifications)) {
            Toast.makeText(context, context.getString(R.string.settings_view_saving_failed), Toast.LENGTH_SHORT).show();
        } else {
            currentSettings.setSyncNotifications(syncNotifications);
            if (syncNotifications) {
                scheduleAppointmentNotificationsJob();
            } else {
                cancelAppointmentNotificationsJob();
            }
        }
    }

    @Override
    public void setSyncAutomatically(Boolean syncAutomatically) {
        if (!settingsService.setSyncAutomatically(syncAutomatically)) {
            Toast.makeText(context, context.getString(R.string.settings_view_saving_failed), Toast.LENGTH_SHORT).show();
        } else {
            currentSettings.setSyncAutomatically(syncAutomatically);
            if (syncAutomatically) {
                scheduleAutoUpdateJob();
            } else {
                cancelAutoUpdateJob();
            }
        }
    }

    @Override
    public void setSyncCalendar(Boolean syncCalendar) {
        if (!settingsService.setSyncCalendar(syncCalendar)) {
            Toast.makeText(context, context.getString(R.string.settings_view_saving_failed), Toast.LENGTH_SHORT).show();
        } else {
            currentSettings.setSyncCalendar(syncCalendar);
            if (syncCalendar) {
                syncWithCalendar();
            }
        }
    }

    @Override
    public void setSelectedCourse(CourseDto course) {
        if (!settingsService.setSelectedCourse(course)) {
            Toast.makeText(context, context.getString(R.string.settings_view_saving_failed), Toast.LENGTH_SHORT).show();
        } else {
            currentSettings.setSelectedCourse(course);
            settingsService.removeAllFilteredAppointment();
            currentFiltered = settingsService.getFiltered();
            view.loadFiltered(currentSettings, currentFiltered);
        }
    }

    @Override
    public void setPartialCourse(CourseDto course) {
        if (!settingsService.setPartialCourse(course)) {
            Toast.makeText(context, context.getString(R.string.settings_view_saving_failed), Toast.LENGTH_SHORT).show();
        } else {
            currentSettings.setPartialCourse(course);
            currentPartials = settingsService.getPartials(course);
            view.loadPartials(currentSettings, currentPartials);
        }
    }

    @Override
    public void addPartialAppointment(CourseDto course, String appointment) {
        if (!settingsService.addPartialAppointment(course, appointment)) {
            Toast.makeText(context, context.getString(R.string.settings_view_saving_failed), Toast.LENGTH_SHORT).show();
        } else {
            currentPartials.add(new PartialCourseDto(course, appointment));
        }
    }

    @Override
    public void removePartialAppointment(CourseDto course, String appointment) {
        if (!settingsService.removePartialAppointment(course, appointment)) {
            Toast.makeText(context, context.getString(R.string.settings_view_saving_failed), Toast.LENGTH_SHORT).show();
        } else {
            currentPartials.remove(new PartialCourseDto(course, appointment));
        }
    }

    @Override
    public void addFilteredAppointment(String appointment) {
        if (!settingsService.addFilteredAppointment(appointment)) {
            Toast.makeText(context, context.getString(R.string.settings_view_saving_failed), Toast.LENGTH_SHORT).show();
        } else {
            currentFiltered.add(new FilteredCourseDto(appointment));
        }
    }

    @Override
    public void removeFilteredAppointment(String appointment) {
        if (!settingsService.removeFilteredAppointment(appointment)) {
            Toast.makeText(context, context.getString(R.string.settings_view_saving_failed), Toast.LENGTH_SHORT).show();
        } else {
            currentFiltered.remove(new FilteredCourseDto(appointment));
        }
    }

    @Override
    public CourseFilterDto getCoursesFilter() {
        CourseFilterDto courseFilterDto = new CourseFilterDto();
        String lastSyncString = sharedPreferences.getString(Utils.SETTINGS_LAST_SYNC_COURSES, null);
        if (lastSyncString != null) {
            courseFilterDto.setLastSync(DateTime.parse(lastSyncString));
        } else {
            courseFilterDto.setLastSync(null);
        }
        return courseFilterDto;
    }

    @Override
    @SuppressLint("MissingPermission")
    public void syncWithCalendar() {
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
                if (sharedPreferences.getString(Utils.SETTINGS_CALENDAR_GUID, null) == null) {
                    editor.putString(Utils.SETTINGS_CALENDAR_GUID, Utils.CALENDAR_SYNC_URI);
                }
                editor.putInt(Utils.SETTINGS_CALENDAR_SYNC_ID, cursor.getInt(PROJECTION_ID_INDEX));
                editor.apply();
                cursor.close();
            } else {
                String selectionOther =
                        "("
                                + CalendarContract.Calendars.IS_PRIMARY + " = ?)";
                String[] selectionOtherArgs = new String[]{"1"};
                Cursor cursorOther = contentResolver.query(uri, EVENT_PROJECTION, selectionOther, selectionOtherArgs, null);
                if (cursorOther != null && cursorOther.moveToFirst()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (sharedPreferences.getString(Utils.SETTINGS_CALENDAR_GUID, null) == null) {
                        editor.putString(Utils.SETTINGS_CALENDAR_GUID, Utils.CALENDAR_SYNC_URI);
                    }
                    editor.putInt(Utils.SETTINGS_CALENDAR_SYNC_ID, cursorOther.getInt(PROJECTION_ID_INDEX));
                    editor.apply();
                    cursorOther.close();
                }
                Toast.makeText(context, context.getString(R.string.settings_view_list_value_calendar_sync_not_found), Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void scheduleAutoUpdateJob() {
        AutoUpdateJob.scheduleJob();
    }

    @Override
    public void cancelAutoUpdateJob() {
        JobManager.instance().cancelAllForTag(Utils.AUTO_UPDATE_JOB_TAG);
    }

    @Override
    public void scheduleAppointmentNotificationsJob() {
        cancelAppointmentNotificationsJob();
        appointmentService
                .scheduleNotifications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void cancelAppointmentNotificationsJob() {
        JobManager.instance().cancelAllForTag(Utils.APPOINTEMENT_NOTIFICATIONS_JOB_TAG);
    }
}
