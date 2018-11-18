package com.idiotnation.raspored.contracts;

import android.content.Context;

import com.idiotnation.raspored.models.dto.AppointmentDto;
import com.idiotnation.raspored.models.dto.AppointmentFilterDto;
import com.idiotnation.raspored.models.dto.CalendarFilterDto;

import java.sql.SQLException;
import java.util.List;

public class MainContract {

    public interface View {
        void initialize();
        void loadList(List<AppointmentDto> appointments);
        void scrollToNow();
        void setRefreshing(boolean isRefreshing);
        void startFirstTimeConfiguration();
    }

    public interface Presenter {
        void start(View view, Context context) throws SQLException;
        boolean checkIfCourseIsSelected();
        void checkSettingsResultFlags(int flags);
        AppointmentFilterDto getAppointmentsFilter();
        CalendarFilterDto getCalendarFilter();
        void getAppointments();
        void syncAppointments();
        void scheduleAutoUpdateJob();
        void cancelAutoUpdateJob();
        void scheduleAppointmentNotificationsJob();
        void cancelAppointmentNotificationsJob();
        void blockAppointment(AppointmentDto appointmentDto);
    }

}
