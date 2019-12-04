package com.idiotnation.raspored.services;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CalendarContract;

import com.idiotnation.raspored.dataaccess.api.ServiceGenerator;
import com.idiotnation.raspored.dataaccess.database.DatabaseManager;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.helpers.exceptions.ServerUnavailableException;
import com.idiotnation.raspored.jobs.AppointmentNotificationJob;
import com.idiotnation.raspored.models.db.Appointment;
import com.idiotnation.raspored.models.db.Course;
import com.idiotnation.raspored.models.dto.AppointmentDto;
import com.idiotnation.raspored.models.dto.AppointmentFilterDto;
import com.idiotnation.raspored.models.dto.AppointmentSyncDto;
import com.idiotnation.raspored.models.dto.CalendarFilterDto;
import com.j256.ormlite.dao.Dao;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import retrofit2.Response;

public class AppointmentService {
    private Dao<Appointment, Integer> appointmentDao;
    private Dao<Course, Integer> courseDao;
    private com.idiotnation.raspored.dataaccess.api.AppointmentService appointmentService;

    public AppointmentService() {
        appointmentDao = DatabaseManager.appointmentDao;
        courseDao = DatabaseManager.courseDao;
        appointmentService = ServiceGenerator.createService(com.idiotnation.raspored.dataaccess.api.AppointmentService.class);
    }

    public Single<List<AppointmentDto>> getAll() {
        return Single
                .fromCallable(new Callable<List<Appointment>>() {
                    @Override
                    public List<Appointment> call() throws Exception {
                        return appointmentDao.queryBuilder()
                                .orderBy("start", true)
                                .query();
                    }
                })
                .map(new Function<List<Appointment>, List<AppointmentDto>>() {
                    @Override
                    public List<AppointmentDto> apply(List<Appointment> appointments) {
                        return Utils.convertToDto(appointments, AppointmentDto.class);
                    }
                });
    }

    private AppointmentSyncDto getAppointmentsFromServer(AppointmentFilterDto appointmentFilter) throws Exception {
        Response<List<AppointmentDto>> response = appointmentService
                .getLatestSynchronous(appointmentFilter)
                .execute();
        int responseCode = response.code();
        if (responseCode == 210) {
            return null;
        } else if (!response.isSuccessful()) {
            throw new ServerUnavailableException();
        }
        final List<AppointmentDto> synced = response.body();
        if (synced != null) {
            appointmentDao.callBatchTasks(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    appointmentDao
                            .deleteBuilder()
                            .delete();
                    for (AppointmentDto appointmentDto : synced) {
                        appointmentDao.create(appointmentDto.toPojo());
                    }
                    return null;
                }
            });
            return new AppointmentSyncDto(synced, responseCode == 211);
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    private void syncCalendarEvents(CalendarFilterDto calendarFilterDto, List<AppointmentDto> appointments, Context context) {
        if (calendarFilterDto.getSyncId() != null) {
            ContentResolver contentResolver = context.getContentResolver();
            String where = "(" + CalendarContract.Events.CUSTOM_APP_URI + " = ?)";
            String[] whereArgs = new String[]{calendarFilterDto.getSyncId()};
            contentResolver.delete(CalendarContract.Events.CONTENT_URI, where, whereArgs);
            if (calendarFilterDto.getCalendarId() != -1) {
                for (AppointmentDto appointment : appointments) {
                    ContentValues eventValues = new ContentValues();
                    eventValues.put(CalendarContract.Events.DTSTART, appointment.getStart().getMillis());
                    eventValues.put(CalendarContract.Events.DTEND, appointment.getEnd().getMillis());
                    eventValues.put(CalendarContract.Events.TITLE, appointment.getName() + ", " + appointment.getClassroom());
                    eventValues.put(CalendarContract.Events.DESCRIPTION, appointment.getDetails() + ", " + appointment.getLecturer());
                    eventValues.put(CalendarContract.Events.CALENDAR_ID, calendarFilterDto.getCalendarId());
                    eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Sarajevo");
                    eventValues.put(CalendarContract.Events.GUESTS_CAN_MODIFY, 0);
                    eventValues.put(CalendarContract.Events.CUSTOM_APP_PACKAGE, context.getPackageName());
                    eventValues.put(CalendarContract.Events.CUSTOM_APP_URI, calendarFilterDto.getSyncId());
                    Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues);
                    if (uri != null) {
                        long eventID = Long.parseLong(uri.getLastPathSegment());
                        ContentValues reminderValues = new ContentValues();
                        reminderValues.put(CalendarContract.Reminders.MINUTES, 30);
                        reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
                        reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                        contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);
                    }
                }
            }
        }
    }

    public Single<AppointmentSyncDto> syncLatest(final AppointmentFilterDto appointmentFilter, final CalendarFilterDto calendarFilterDto, final Context context) {
        return Single
                .fromCallable(new Callable<AppointmentSyncDto>() {
                    @Override
                    public AppointmentSyncDto call() throws Exception {
                        DateTime lastSync = appointmentFilter.getLastSync();
                        if (lastSync != null) {
                            appointmentFilter.setLastSync(lastSync.withZone(DateTimeZone.UTC));
                        }
                        AppointmentSyncDto appointmentSync = getAppointmentsFromServer(appointmentFilter);
                        if (appointmentSync != null) {
                            syncCalendarEvents(calendarFilterDto, appointmentSync.getAppointments(), context);
                            return appointmentSync;
                        }
                        return new AppointmentSyncDto(
                                Utils.convertToDto(
                                        appointmentDao
                                                .queryBuilder()
                                                .orderBy("start", true)
                                                .query()
                                        , AppointmentDto.class),
                                false
                        );
                    }
                });
    }

    public Single<Boolean> autoUpdateSync(final AppointmentFilterDto appointmentFilter, final CalendarFilterDto calendarFilterDto, final Context context) {
        return Single
                .fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        try {
                            AppointmentSyncDto appointmentSync = getAppointmentsFromServer(appointmentFilter);
                            if (appointmentSync != null && appointmentSync.getAppointments() != null && !appointmentSync.getOutOfSync()) {
                                syncCalendarEvents(calendarFilterDto, appointmentSync.getAppointments(), context);
                                return true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                });
    }

    public Single scheduleNotifications() {
        return Single
                .fromCallable(new Callable() {

                    @Override
                    public Object call() throws Exception {
                        List<Appointment> appointments = appointmentDao.queryBuilder()
                                .orderBy("start", true)
                                .query();
                        for (Appointment appointment : appointments) {
                            AppointmentNotificationJob.scheduleJob(appointment.getStart().minusMinutes(30).getMillis());
                        }
                        return null;
                    }
                });
    }
}
