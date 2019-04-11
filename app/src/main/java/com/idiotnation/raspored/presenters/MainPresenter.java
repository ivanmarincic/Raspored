package com.idiotnation.raspored.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;
import android.widget.Toast;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.contracts.MainContract;
import com.idiotnation.raspored.models.dto.AppointmentDto;
import com.idiotnation.raspored.models.dto.AppointmentFilterDto;
import com.idiotnation.raspored.models.dto.CalendarFilterDto;
import com.idiotnation.raspored.models.dto.SettingsItemDto;
import com.idiotnation.raspored.services.AppointmentService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainPresenter implements MainContract.Presenter {

    private MainContract.View view;
    private Context context;
    private SharedPreferences sharedPreferences;
    private AppointmentService appointmentService;

    public MainPresenter() {
    }

    @Override
    public void start(MainContract.View view, Context context) {
        this.view = view;
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        appointmentService = new AppointmentService();
        if (view != null) {
            view.initialize();
        }
    }

    @Override
    public boolean checkIfCourseIsSelected() {
        Integer selectedCourse = sharedPreferences.getInt(SettingsItemDto.SETTINGS_TYPE_COURSE, -1);
        if (selectedCourse == -1) {
            view.startFirstTimeConfiguration();
            return false;
        }
        return true;
    }

    @Override
    public AppointmentFilterDto getAppointmentsFilter() {
        AppointmentFilterDto appointmentFilterDto = new AppointmentFilterDto();
        appointmentFilterDto.setLastSync(null);
        appointmentFilterDto.setCourseId(sharedPreferences.getInt(SettingsItemDto.SETTINGS_TYPE_COURSE, -1));
        appointmentFilterDto.setPartialCourseId(sharedPreferences.getInt(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE, -1));
        appointmentFilterDto.setPartialStrings(new ArrayList<>(sharedPreferences.getStringSet(SettingsItemDto.SETTINGS_TYPE_PARTIAL, new HashSet<String>())));
        appointmentFilterDto.setBlockedStrings(new ArrayList<>(sharedPreferences.getStringSet(SettingsItemDto.SETTINGS_TYPE_BLOCKED, new HashSet<String>())));
        return appointmentFilterDto;
    }

    @Override
    public CalendarFilterDto getCalendarFilter() {
        CalendarFilterDto calendarFilterDto = new CalendarFilterDto();
        calendarFilterDto.setCalendarId(sharedPreferences.getInt(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC_ID, -1));
        calendarFilterDto.setSyncId(sharedPreferences.getString(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC_UUID, null));
        return calendarFilterDto;
    }

    @Override
    public void getAppointments() {
        appointmentService
                .getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<AppointmentDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<AppointmentDto> appointments) {
                        view.loadList(appointments);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof IOException) {
                            Toast.makeText(context, context.getResources().getString(R.string.request_error_internet), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, context.getResources().getString(R.string.request_error_internal), Toast.LENGTH_SHORT).show();
                        }
                        view.setRefreshing(false);
                    }
                });
    }

    @Override
    public void syncAppointments() {
        appointmentService
                .syncLatest(getAppointmentsFilter(), getCalendarFilter(), context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Pair<List<AppointmentDto>, Boolean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        view.setRefreshing(true);
                    }

                    @Override
                    public void onSuccess(Pair<List<AppointmentDto>, Boolean> appointments) {
                        if (appointments.second) {
                            sharedPreferences
                                    .edit()
                                    .putString(SettingsItemDto.SETTINGS_TYPE_LAST_SYNC, DateTime.now().withZone(DateTimeZone.UTC).toString())
                                    .apply();
                        }
                        view.loadList(appointments.first);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof IOException) {
                            Toast.makeText(context, context.getResources().getString(R.string.request_error_internet), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, context.getResources().getString(R.string.request_error_internal), Toast.LENGTH_SHORT).show();
                        }
                        view.setRefreshing(false);
                    }
                });
    }

    @Override
    public void blockAppointment(AppointmentDto appointmentDto) {
        if (appointmentDto != null && appointmentDto.getName() != null) {
            Set<String> currentSet = sharedPreferences.getStringSet(SettingsItemDto.SETTINGS_TYPE_BLOCKED, new HashSet<String>());
            currentSet.add(appointmentDto.getName());
            sharedPreferences
                    .edit()
                    .putStringSet(SettingsItemDto.SETTINGS_TYPE_BLOCKED, currentSet)
                    .apply();
        }
        syncAppointments();
    }
}
