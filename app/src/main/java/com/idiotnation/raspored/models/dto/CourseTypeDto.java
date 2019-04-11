package com.idiotnation.raspored.models.dto;

import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.db.Course;
import com.idiotnation.raspored.models.db.CourseType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CourseTypeDto {
    private Integer id = null;
    private String name = "";
    private List<CourseDto> courses;

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

    public List<CourseDto> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseDto> courses) {
        this.courses = courses;
    }

    public CourseTypeDto() {
    }

    public CourseTypeDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public CourseTypeDto(CourseType courseType) {
        this(courseType, false);
    }

    public CourseTypeDto(CourseType courseType, Boolean recursive) {
        this.id = courseType.getId();
        this.name = courseType.getName();
        if (courseType.getCourses() != null && !recursive) {
            Iterator<Course> iterator = courseType.getCourses().iterator();
            this.courses = new ArrayList<>();
            while (iterator.hasNext()) {
                this.courses.add(new CourseDto(iterator.next(), true));
            }
        }
    }

    public CourseType toPojo() {
        CourseType courseType = new CourseType();
        courseType.setId(id);
        courseType.setName(name);
        return courseType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseTypeDto that = (CourseTypeDto) o;
        return Utils.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Utils.hash(name);
    }
}
