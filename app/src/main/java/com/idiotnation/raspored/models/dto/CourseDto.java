package com.idiotnation.raspored.models.dto;

import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.db.Course;

import org.joda.time.DateTime;

public class CourseDto {
    private Integer id = null;
    private String name = "";
    private String url = "";
    private CourseTypeDto type = new CourseTypeDto();
    private Integer year = -1;
    private DateTime lastSync = null;

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

    public CourseDto() {
    }

    public CourseDto(Integer id, String name, String url, CourseTypeDto type, Integer year, DateTime lastSync) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.type = type;
        this.year = year;
        this.lastSync = lastSync;
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
}
