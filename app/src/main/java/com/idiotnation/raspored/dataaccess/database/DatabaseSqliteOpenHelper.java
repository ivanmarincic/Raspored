package com.idiotnation.raspored.dataaccess.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.models.db.Appointment;
import com.idiotnation.raspored.models.db.Course;
import com.idiotnation.raspored.models.db.CourseType;
import com.idiotnation.raspored.models.db.FilteredCourse;
import com.idiotnation.raspored.models.db.PartialCourse;
import com.idiotnation.raspored.models.db.Settings;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseSqliteOpenHelper extends OrmLiteSqliteOpenHelper {

    private static final String DB_NAME = "raspored-sync.db";
    private static final Integer DB_VERSION = 4;

    public DatabaseSqliteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, CourseType.class);
            TableUtils.createTable(connectionSource, Course.class);
            TableUtils.createTable(connectionSource, Appointment.class);
            TableUtils.createTable(connectionSource, Settings.class);
            TableUtils.createTable(connectionSource, PartialCourse.class);
            TableUtils.createTable(connectionSource, FilteredCourse.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, CourseType.class, true);
            TableUtils.dropTable(connectionSource, Course.class, true);
            TableUtils.dropTable(connectionSource, Appointment.class, true);
            TableUtils.dropTable(connectionSource, Settings.class, true);
            TableUtils.dropTable(connectionSource, PartialCourse.class, true);
            TableUtils.dropTable(connectionSource, FilteredCourse.class, true);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            onCreate(database, connectionSource);
        }
    }
}
