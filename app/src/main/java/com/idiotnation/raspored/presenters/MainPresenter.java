package com.idiotnation.raspored.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.contracts.MainContract;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.helpers.exceptions.ServerUnavailableException;
import com.idiotnation.raspored.models.dto.AppointmentDto;
import com.idiotnation.raspored.models.dto.AppointmentFilterDto;
import com.idiotnation.raspored.models.dto.AppointmentSyncDto;
import com.idiotnation.raspored.models.dto.CalendarFilterDto;
import com.idiotnation.raspored.models.dto.FilteredCourseDto;
import com.idiotnation.raspored.models.dto.PartialCourseDto;
import com.idiotnation.raspored.models.dto.SettingsDto;
import com.idiotnation.raspored.services.AppointmentService;
import com.idiotnation.raspored.services.SettingsService;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainPresenter implements MainContract.Presenter {

    private MainContract.View view;
    private Context context;
    private SharedPreferences sharedPreferences;
    private AppointmentService appointmentService;
    private SettingsService settingsService;
    private SettingsDto currentSettings;
    private List<PartialCourseDto> currentPartials;
    private List<FilteredCourseDto> currentFiltered;

    public MainPresenter() {
    }

    @Override
    public void start(MainContract.View view, Context context) {
        this.view = view;
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        appointmentService = new AppointmentService();
        settingsService = new SettingsService();
        if (view != null) {
            view.initialize();
        }
    }

    @Override
    public boolean checkIfCourseIsSelected() {
        currentSettings = settingsService.getSettings();
        if (currentSettings == null) {
            currentSettings = settingsService.createSettings();
        }
        currentPartials = settingsService.getPartials(currentSettings.getPartialCourse());
        currentFiltered = settingsService.getFiltered();
        if (currentSettings.getSelectedCourse() == null) {
            view.startFirstTimeConfiguration();
            return false;
        }
        return true;
    }

    @Override
    public AppointmentFilterDto getAppointmentsFilter() {
        AppointmentFilterDto appointmentFilterDto = new AppointmentFilterDto();
        appointmentFilterDto.setLastSync(null);
        appointmentFilterDto.setCourseId(currentSettings.getSelectedCourse().getId());
        appointmentFilterDto.setPartialCourseId(currentSettings.getPartialCourse() != null ? currentSettings.getPartialCourse().getId() : -1);
        appointmentFilterDto.setPartialStrings(Utils.listToStringList(currentPartials));
        appointmentFilterDto.setBlockedStrings(Utils.listToStringList(currentFiltered));
        return appointmentFilterDto;
    }

    @Override
    public CalendarFilterDto getCalendarFilter() {
        CalendarFilterDto calendarFilterDto = new CalendarFilterDto();
        calendarFilterDto.setCalendarId(sharedPreferences.getInt(Utils.SETTINGS_CALENDAR_SYNC_ID, -1));
        calendarFilterDto.setSyncId(sharedPreferences.getString(Utils.SETTINGS_CALENDAR_GUID, null));
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
                        syncAppointments();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof IOException) {
                            Toast.makeText(context, context.getResources().getString(R.string.request_error_internet), Toast.LENGTH_SHORT).show();
                        } else if (e instanceof ServerUnavailableException) {
                            Toast.makeText(context, context.getResources().getString(R.string.request_error_server), Toast.LENGTH_SHORT).show();
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
                .subscribe(new SingleObserver<AppointmentSyncDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        view.setRefreshing(true);
                    }

                    @Override
                    public void onSuccess(AppointmentSyncDto appointmentSync) {
                        sharedPreferences
                                .edit()
                                .putString(Utils.SETTINGS_LAST_SYNC, DateTime.now().toString())
                                .apply();
                        view.loadList(appointmentSync.getAppointments());
                        if (appointmentSync.getOutOfSync()) {
                            Toast.makeText(context, context.getResources().getString(R.string.request_error_out_of_sync), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof IOException) {
                            Toast.makeText(context, context.getResources().getString(R.string.request_error_internet), Toast.LENGTH_SHORT).show();
                        } else if (e instanceof ServerUnavailableException) {
                            Toast.makeText(context, context.getResources().getString(R.string.request_error_server), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, context.getResources().getString(R.string.request_error_internal), Toast.LENGTH_SHORT).show();
                        }
                        view.setRefreshing(false);
                    }
                });
    }

    @Override
    public String currentURL() {
        return currentSettings.getSelectedCourse().getUrl();
    }

    @Override
    public void blockAppointment(AppointmentDto appointmentDto) {
        if (appointmentDto != null && appointmentDto.getName() != null) {
            settingsService.addFilteredAppointment(appointmentDto.getName());
            currentFiltered = settingsService.getFiltered();
            syncAppointments();
        }
    }
}
