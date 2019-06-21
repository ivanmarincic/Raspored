package com.idiotnation.raspored.models.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.db.Course;

import org.joda.time.DateTime;

public class CourseDto implements Parcelable {
    private Integer id = null;
    private String name = "";
    private String url = "";
    private CourseTypeDto type = new CourseTypeDto();
    private Integer year = -1;
    private DateTime lastSync = null;
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

    public CourseTypeDto getType() {
        return type;
    }

    public void setType(CourseTypeDto type) {
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

    public CourseDto() {
    }

    public CourseDto(Integer id, String name, String url, CourseTypeDto type, Integer year, DateTime lastSync, DateTime lastFailed) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.type = type;
        this.year = year;
        this.lastSync = lastSync;
        this.lastFailed = lastFailed;
    }

    public CourseDto(Course course) {
        this(course, false);
    }

    public CourseDto(Course course, Boolean recursive) {
        this.id = course.getId();
        this.name = course.getName();
        this.url = course.getUrl();
        if (course.getType() != null && !recursive) {
            this.type = new CourseTypeDto(course.getType(), true);
        } else {
            this.type = null;
        }
        this.year = course.getYear();
        this.lastSync = course.getLastSync();
        this.lastFailed = course.getLastFailed();
    }

    public Course toPojo() {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        if (type != null) {
            course.setType(type.toPojo());
        } else {
            course.setType(null);
        }
        course.setUrl(url);
        course.setYear(year);
        course.setLastSync(lastSync);
        return course;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseDto courseDto = (CourseDto) o;
        return Utils.equals(name, courseDto.name) &&
                Utils.equals(url, courseDto.url) &&
                Utils.equals(type, courseDto.type) &&
                Utils.equals(year, courseDto.year);
    }

    @Override
    public int hashCode() {
        return Utils.hash(name, url, type, year);
    }

    @Override
    public String toString() {
        return name;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeInt(year);
        dest.writeParcelable(type, 0);
        dest.writeSerializable(lastSync);
        dest.writeSerializable(lastFailed);
    }
}
