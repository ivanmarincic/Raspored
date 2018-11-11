package com.idiotnation.raspored.models.dto;

import org.joda.time.DateTime;

public class AppointmentHeaderDto {
    String value;
    DateTime date;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public AppointmentHeaderDto() {
    }

    public AppointmentHeaderDto(String value, DateTime date) {
        this.value = value;
        this.date = date;
    }
}
