package com.idiotnation.raspored.dataaccess.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.models.jpa.Appointment;
import com.idiotnation.raspored.models.jpa.Course;
import com.idiotnation.raspored.models.jpa.CourseType;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseSqliteOpenHelper extends OrmLiteSqliteOpenHelper {

    private static final String DB_NAME = "raspored-sync.db";
    private static final Integer DB_VERSION = 2;

    public DatabaseSqliteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, CourseType.class);
            TableUtils.createTable(connectionSource, Course.class);
            TableUtils.createTable(connectionSource, Appointment.class);
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
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            onCreate(database, connectionSource);
        }
    }
}
