package com.idiotnation.raspored.services;

import com.idiotnation.raspored.dataaccess.database.DatabaseManager;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.db.FilteredCourse;
import com.idiotnation.raspored.models.db.PartialCourse;
import com.idiotnation.raspored.models.db.Settings;
import com.idiotnation.raspored.models.dto.CourseDto;
import com.idiotnation.raspored.models.dto.FilteredCourseDto;
import com.idiotnation.raspored.models.dto.PartialCourseDto;
import com.idiotnation.raspored.models.dto.SettingsDto;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SettingsService {

    private Dao<Settings, Integer> settingsDao;
    private Dao<PartialCourse, Integer> partialsDao;
    private Dao<FilteredCourse, Integer> filteredDao;

    public SettingsService() {
        settingsDao = DatabaseManager.settingsDao;
        partialsDao = DatabaseManager.partialCourseDao;
        filteredDao = DatabaseManager.filteredCourseDao;
    }

    public SettingsDto getSettings() {
        try {
            Settings settings = settingsDao.queryForId(1);
            if (settings != null) {
                return new SettingsDto(settings);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SettingsDto createSettings() {
        try {
            Settings settings = new Settings();
            settingsDao.create(settings);
            return new SettingsDto(settings);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PartialCourseDto> getPartials(CourseDto course) {
        if (course == null) {
            return new ArrayList<>();
        }
        try {
            return Utils.convertToDto(
                    partialsDao.queryForEq("course_id", course.getId()),
                    PartialCourseDto.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<FilteredCourseDto> getFiltered() {
        try {
            return Utils.convertToDto(
                    filteredDao.queryForAll(),
                    FilteredCourseDto.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean setSyncCalendar(Boolean syncCalendar) {
        try {
            return settingsDao
                    .updateBuilder()
                    .updateColumnValue("sync_calendar", syncCalendar)
                    .update() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setSyncAutomatically(Boolean syncAutomatically) {
        try {
            return settingsDao
                    .updateBuilder()
                    .updateColumnValue("sync_automatically", syncAutomatically)
                    .update() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setSyncNotifications(Boolean syncNotifications) {
        try {
            return settingsDao
                    .updateBuilder()
                    .updateColumnValue("sync_notifications", syncNotifications)
                    .update() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setSelectedCourse(CourseDto course) {
        try {
            return settingsDao
                    .updateBuilder()
                    .updateColumnValue("selected_course_id", course.getId())
                    .update() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setPartialCourse(CourseDto course) {
        try {
            return settingsDao
                    .updateBuilder()
                    .updateColumnValue("partial_course_id", course.getId())
                    .update() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addPartialAppointment(CourseDto partialCourse, String appointment) {
        try {
            return partialsDao.
                    create(new PartialCourse(partialCourse.toPojo(), appointment)) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removePartialAppointment(CourseDto partialCourse, String appointment) {
        try {
            DeleteBuilder builder = partialsDao
                    .deleteBuilder();
            builder
                    .where()
                    .eq("course_id", partialCourse.getId())
                    .and()
                    .eq("name", appointment);
            return builder
                    .delete() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeAllPartialAppointment(CourseDto partialCourse) {
        try {
            return partialsDao
                    .deleteBuilder()
                    .delete() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addFilteredAppointment(String appointment) {
        try {
            return filteredDao.
                    create(new FilteredCourse(appointment)) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFilteredAppointment(String appointment) {
        try {
            DeleteBuilder builder = filteredDao
                    .deleteBuilder();
            builder
                    .where()
                    .eq("name", appointment);
            return builder
                    .delete() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeAllFilteredAppointment() {
        try {
            return filteredDao
                    .deleteBuilder()
                    .delete() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
