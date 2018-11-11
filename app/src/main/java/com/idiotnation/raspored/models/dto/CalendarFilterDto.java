package com.idiotnation.raspored.models.dto;

public class CalendarFilterDto {
    private Integer calendarId;
    private String syncId;

    public Integer getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Integer calendarId) {
        this.calendarId = calendarId;
    }

    public String getSyncId() {
        return syncId;
    }

    public void setSyncId(String syncId) {
        this.syncId = syncId;
    }

    public CalendarFilterDto() {
    }

    public CalendarFilterDto(Integer calendarId, String syncId) {
        this.calendarId = calendarId;
        this.syncId = syncId;
    }
}
