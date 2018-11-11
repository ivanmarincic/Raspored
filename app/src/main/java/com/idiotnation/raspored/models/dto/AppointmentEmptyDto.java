package com.idiotnation.raspored.models.dto;

public class AppointmentEmptyDto {
    String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AppointmentEmptyDto() {
    }

    public AppointmentEmptyDto(String value) {
        this.value = value;
    }
}
