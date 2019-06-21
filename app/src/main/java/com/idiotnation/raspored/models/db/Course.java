package com.idiotnation.raspored.models.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

@DatabaseTable(tableName = "courses")
public class Course {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private Integer id = null;

    @DatabaseField(columnName = "name", unique = true)
    private String name = "";

    @DatabaseField(columnName = "url")
    private String url = "";

    @DatabaseField(columnName = "course_type_id", foreign = true, foreignAutoRefresh = true)
    private CourseType type = null;

    @DatabaseField(columnName = "year")
    private Integer year = -1;

    @DatabaseField(columnName = "last_sync", dataType = DataType.DATE_TIME)
    private DateTime lastSync = null;

    @DatabaseField(columnName = "last_failed", dataType = DataType.DATE_TIME)
    private DateTime lastFailed = null;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public CourseType getType() {
        return type;
    }

    public void setType(CourseType type) {
        this.type = type;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public DateTime getLastSync() {
        return lastSync;
    }

    public void setLastSync(DateTime lastSync) {
        this.lastSync = lastSync;
    }

    public DateTime getLastFailed() {
        return lastFailed;
    }

    public void setLastFailed(DateTime lastFailed) {
        this.lastFailed = lastFailed;
    }

    public Course() {
    }

    public Course(Integer id) {
        this.id = id;
    }

    public Course(Integer id, String name, String url, CourseType type, Integer year, DateTime lastSync, DateTime lastFailed) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.type = type;
        this.year = year;
        this.lastSync = lastSync;
        this.lastFailed = lastFailed;
    }
}
