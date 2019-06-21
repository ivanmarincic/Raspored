package com.idiotnation.raspored.models.dto;

import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.db.Settings;

public class SettingsDto {

    private Integer id = 1;
    private CourseDto selectedCourse = null;
    private CourseDto partialCourse = null;
    private Boolean syncNotifications = false;
    private Boolean syncAutomatically = false;
    private Boolean syncCalendar = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CourseDto getSelectedCourse() {
        return selectedCourse;
    }

    public void setSelectedCourse(CourseDto selectedCourse) {
        this.selectedCourse = selectedCourse;
    }

    public CourseDto getPartialCourse() {
        return partialCourse;
    }

    public void setPartialCourse(CourseDto partialCourse) {
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

    public SettingsDto() {
    }

    public SettingsDto(Integer id, CourseDto selectedCourse, CourseDto partialCourse, Boolean syncNotifications, Boolean syncAutomatically, Boolean syncCalendar) {
        this.id = id;
        this.selectedCourse = selectedCourse;
        this.partialCourse = partialCourse;
        this.syncNotifications = syncNotifications;
        this.syncAutomatically = syncAutomatically;
        this.syncCalendar = syncCalendar;
    }

    public SettingsDto(Settings settings) {
        this(settings, false);
    }

    public SettingsDto(Settings settings, Boolean recursive) {
        this.id = settings.getId();
        this.syncAutomatically = settings.getSyncAutomatically();
        this.syncCalendar = settings.getSyncCalendar();
        this.syncNotifications = settings.getSyncNotifications();
        if (settings.getSelectedCourse() != null && !recursive) {
            this.selectedCourse = new CourseDto(settings.getSelectedCourse(), true);
        } else {
            this.selectedCourse = null;
        }
        if (settings.getPartialCourse() != null && !recursive) {
            this.partialCourse = new CourseDto(settings.getPartialCourse(), true);
        } else {
            this.partialCourse = null;
        }
    }

    public Settings toPojo() {
        Settings settings = new Settings();
        settings.setId(id);
        settings.setSyncAutomatically(syncAutomatically);
        settings.setSyncCalendar(syncCalendar);
        settings.setSyncNotifications(syncNotifications);
        if (selectedCourse != null) {
            settings.setSelectedCourse(selectedCourse.toPojo());
        } else {
            settings.setSelectedCourse(null);
        }
        return settings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsDto that = (SettingsDto) o;
        return Utils.equals(id, that.id) &&
                Utils.equals(selectedCourse, that.selectedCourse) &&
                Utils.equals(partialCourse, that.partialCourse) &&
                Utils.equals(syncNotifications, that.syncNotifications) &&
                Utils.equals(syncAutomatically, that.syncAutomatically) &&
                Utils.equals(syncCalendar, that.syncCalendar);
    }

    @Override
    public int hashCode() {
        return Utils.hash(id, selectedCourse, partialCourse, syncNotifications, syncAutomatically, syncCalendar);
    }

    @Override
    public String toString() {
        return "SettingsDto{" +
                "id=" + id +
                ", selectedCourse=" + selectedCourse +
                ", partialCourse=" + partialCourse +
                ", syncNotifications=" + syncNotifications +
                ", syncAutomatically=" + syncAutomatically +
                ", syncCalendar=" + syncCalendar +
                '}';
    }
}
