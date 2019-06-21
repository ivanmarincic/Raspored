package com.idiotnation.raspored.contracts;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.idiotnation.raspored.models.dto.CourseDto;
import com.idiotnation.raspored.models.dto.CourseFilterDto;
import com.idiotnation.raspored.models.dto.FilteredCourseDto;
import com.idiotnation.raspored.models.dto.PartialCourseDto;
import com.idiotnation.raspored.models.dto.SettingsDto;

import java.sql.SQLException;
import java.util.List;

public class SettingsContract {
    public interface View {
        void initialize();

        void loadSettings(SettingsDto settings);

        void loadPartials(SettingsDto settings, List<PartialCourseDto> partials);

        void loadFiltered(SettingsDto settings, List<FilteredCourseDto> filtered);

        void hideLoading();

        AppCompatActivity getActivity();
    }

    public interface Presenter {
        void start(View view, Context context) throws SQLException;

        SettingsDto getSettings();

        List<PartialCourseDto> getPartials();

        List<FilteredCourseDto> getFiltered();

        void loadSettings();

        void setSyncNotifications(Boolean syncNotifications);

        void setSyncAutomatically(Boolean syncAutomatically);

        void setSyncCalendar(Boolean syncCalendar);

        void setSelectedCourse(CourseDto course);

        void setPartialCourse(CourseDto course);

        void addPartialAppointment(CourseDto course, String appointment);

        void removePartialAppointment(CourseDto course, String appointment);

        void addFilteredAppointment(String appointment);

        void removeFilteredAppointment(String appointment);

        CourseFilterDto getCoursesFilter();

        void syncWithCalendar();

        void scheduleAutoUpdateJob();

        void cancelAutoUpdateJob();

        void scheduleAppointmentNotificationsJob();

        void cancelAppointmentNotificationsJob();
    }
}
