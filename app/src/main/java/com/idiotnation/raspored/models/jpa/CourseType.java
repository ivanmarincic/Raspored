package com.idiotnation.raspored.models.jpa;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "course_types")
public class CourseType {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    Integer id = null;

    @DatabaseField(columnName = "name", unique = true)
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

    public CourseType() {
    }

    public CourseType(Integer id) {
        this.id = id;
    }

    public CourseType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
