package com.idiotnation.raspored.models.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "settings")
public class Settings {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private Integer id = 1;

    @DatabaseField(columnName = "selected_course_id", foreign = true, foreignAutoRefresh = true)
    private Course selectedCourse = null;

    @DatabaseField(columnName = "partial_course_id", foreign = true, foreignAutoRefresh = true)
    private Course partialCourse = null;

    @DatabaseField(columnName = "sync_notifications")
    private Boolean syncNotifications = false;

    @DatabaseField(columnName = "sync_automatically")
    private Boolean syncAutomatically = false;

    @DatabaseField(columnName = "sync_calendar")
    private Boolean syncCalendar = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Course getSelectedCourse() {
        return selectedCourse;
    }

    public void setSelectedCourse(Course selectedCourse) {
        this.selectedCourse = selectedCourse;
    }

    public Course getPartialCourse() {
        return partialCourse;
    }

    public void setPartialCourse(Course partialCourse) {
        this.partialCourse = partialCourse;
    }

    public Boolean getSyncNotifications() {
        return syncNotifications;
    }

    public void setSyncNotifications(Boolean syncNotifications) {
        this.syncNotifications = syncNotifications;
    }

    public Boolean getSyncAutomatically() {
        return syncAutomatically;
    }

    public void setSyncAutomatically(Boolean syncAutomatically) {
        this.syncAutomatically = syncAutomatically;
    }

    public Boolean getSyncCalendar() {
        return syncCalendar;
    }

    public void setSyncCalendar(Boolean syncCalendar) {
        this.syncCalendar = syncCalendar;
    }

    public Settings() {
        id = 1;
        selectedCourse = null;
        partialCourse = null;
        syncAutomatically = false;
        syncCalendar  = false;
        syncNotifications  = false;
    }

    public Settings(Integer id, Course selectedCourse, Course partialCourse, Boolean syncNotifications, Boolean syncAutomatically, Boolean syncCalendar) {
        this.id = id;
        this.selectedCourse = selectedCourse;
        this.partialCourse = partialCourse;
        this.syncNotifications = syncNotifications;
        this.syncAutomatically = syncAutomatically;
        this.syncCalendar = syncCalendar;
    }
}
