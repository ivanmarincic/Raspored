package com.idiotnation.raspored.models.dto;

import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.jpa.CourseType;

public class CourseTypeDto {
    Integer id = null;
    String name = "";

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

    public CourseTypeDto() {
    }

    public CourseTypeDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public CourseTypeDto(CourseType courseType) {
        this.id = courseType.getId();
        this.name = courseType.getName();
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
