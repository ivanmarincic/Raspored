package com.idiotnation.raspored.models.dto;

import java.util.List;

public class AppointmentSyncDto {
    private List<AppointmentDto> appointments;
    private Boolean outOfSync;

    public List<AppointmentDto> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<AppointmentDto> appointments) {
        this.appointments = appointments;
    }

    public Boolean getOutOfSync() {
        return outOfSync;
    }

    public void setOutOfSync(Boolean outOfSync) {
        this.outOfSync = outOfSync;
    }

    public AppointmentSyncDto() {
    }

    public AppointmentSyncDto(List<AppointmentDto> appointments, Boolean outOfSync) {
        this.appointments = appointments;
        this.outOfSync = outOfSync;
    }
}
