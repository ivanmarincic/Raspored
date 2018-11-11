package com.idiotnation.raspored.models.dto;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class AppointmentFilterDto {
    Integer courseId = -1;
    DateTime lastSync = null;
    Integer partialCourseId = -1;
    List<String> partialStrings = new ArrayList<>();
    List<String> blockedStrings = new ArrayList<>();

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Integer getPartialCourseId() {
        return partialCourseId;
    }

    public void setPartialCourseId(Integer partialCourseId) {
        this.partialCourseId = partialCourseId;
    }

    public List<String> getPartialStrings() {
        return partialStrings;
    }

    public void setPartialStrings(List<String> partialStrings) {
        this.partialStrings = partialStrings;
    }

    public List<String> getBlockedStrings() {
        return blockedStrings;
    }

    public void setBlockedStrings(List<String> blockedStrings) {
        this.blockedStrings = blockedStrings;
    }

    public DateTime getLastSync() {
        return lastSync;
    }

    public void setLastSync(DateTime lastSync) {
        this.lastSync = lastSync;
    }

    public AppointmentFilterDto() {
    }

    public AppointmentFilterDto(Integer courseId, DateTime lastSync, Integer partialCourseId, List<String> partialStrings, List<String> blockedStrings) {
        this.courseId = courseId;
        this.lastSync = lastSync;
        this.partialCourseId = partialCourseId;
        this.partialStrings = partialStrings;
        this.blockedStrings = blockedStrings;
    }
}
