package com.idiotnation.raspored.models.dto;

import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.db.PartialCourse;

public class PartialCourseDto {
    private Integer id;
    private CourseDto course;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CourseDto getCourse() {
        return course;
    }

    public void setCourse(CourseDto course) {
        this.course = course;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PartialCourseDto() {
    }

    public PartialCourseDto(Integer id) {
        this.id = id;
    }

    public PartialCourseDto(CourseDto course, String name) {
        this.course = course;
        this.name = name;
    }

    public PartialCourseDto(PartialCourse partialCourse) {
        this(partialCourse, false);
    }

    public PartialCourseDto(PartialCourse partialCourse, Boolean recursive) {
        this.id = partialCourse.getId();
        this.name = partialCourse.getName();
        if (partialCourse.getCourse() != null && !recursive) {
            this.course = new CourseDto(partialCourse.getCourse(), true);
        } else {
            this.course = null;
        }
    }

    public PartialCourse toPojo() {
        PartialCourse partialCourse = new PartialCourse();
        partialCourse.setId(id);
        partialCourse.setName(name);
        if (course != null) {
            partialCourse.setCourse(course.toPojo());
        } else {
            partialCourse.setCourse(null);
        }
        return partialCourse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartialCourseDto that = (PartialCourseDto) o;
        return Utils.equals(course, that.course) &&
                Utils.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Utils.hash(id, course, name);
    }

    @Override
    public String toString() {
        return name;
    }
}
