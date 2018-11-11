package com.idiotnation.raspored.models.dto;

import org.joda.time.DateTime;

public class CourseFilterDto {
    DateTime lastSync;

    public DateTime getLastSync() {
        return lastSync;
    }

    public void setLastSync(DateTime lastSync) {
        this.lastSync = lastSync;
    }

    public CourseFilterDto() {
    }

    public CourseFilterDto(DateTime lastSync) {
        this.lastSync = lastSync;
    }
}
