package com.idiotnation.raspored.dataaccess.database;

import com.idiotnation.raspored.Application;
import com.idiotnation.raspored.models.db.Appointment;
import com.idiotnation.raspored.models.db.Course;
import com.idiotnation.raspored.models.db.CourseType;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

public class DatabaseManager {
    private static final DatabaseSqliteOpenHelper helper = Application.getDatabaseHelper();
    public static Dao<Appointment, Integer> appointmentDao;
    public static Dao<Course, Integer> courseDao;
    public static Dao<CourseType, Integer> courseTypeDao;

    static {
        try {
            courseDao = helper.getDao(Course.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            appointmentDao = helper.getDao(Appointment.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            courseTypeDao = helper.getDao(CourseType.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
