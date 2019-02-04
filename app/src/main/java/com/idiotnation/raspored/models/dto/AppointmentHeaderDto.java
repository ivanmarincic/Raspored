package com.idiotnation.raspored.models.dto;

public class AppointmentHeaderDto {
    String dayOfWeek;
    String date;

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public AppointmentHeaderDto() {
    }

    public AppointmentHeaderDto(String dayOfWeek, String date) {
        this.dayOfWeek = dayOfWeek;
        this.date = date;
    }
}
