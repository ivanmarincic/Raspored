package com.idiotnation.raspored.models.dto;

import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.db.FilteredCourse;

public class FilteredCourseDto {

    private Integer id;
    private String name = null;

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

    public FilteredCourseDto() {
    }

    public FilteredCourseDto(Integer id) {
        this.id = id;
    }

    public FilteredCourseDto(String name) {
        this.name = name;
    }

    public FilteredCourseDto(FilteredCourse filteredCourse) {
        this(filteredCourse, false);
    }

    public FilteredCourseDto(FilteredCourse filteredCourse, Boolean recursive) {
        this.id = filteredCourse.getId();
        this.name = filteredCourse.getName();
    }

    public FilteredCourse toPojo() {
        FilteredCourse filteredCourse = new FilteredCourse();
        filteredCourse.setId(id);
        filteredCourse.setName(name);
        return filteredCourse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilteredCourseDto that = (FilteredCourseDto) o;
        return Utils.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Utils.hash(id, name);
    }

    @Override
    public String toString() {
        return name;
    }
}
