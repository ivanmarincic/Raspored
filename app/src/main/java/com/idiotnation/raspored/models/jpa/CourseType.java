package com.idiotnation.raspored.models.jpa;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "course_types")
public class CourseType {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private Integer id = null;

    @DatabaseField(columnName = "name", unique = true)
    private String name = "";

    @ForeignCollectionField(eager = true, orderColumnName = "name")
    private ForeignCollection<Course> courses;

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

    public ForeignCollection<Course> getCourses() {
        return courses;
    }

    public void setCourses(ForeignCollection<Course> courses) {
        this.courses = courses;
    }

    public CourseType() {
    }

    public CourseType(Integer id) {
        this.id = id;
    }

    public CourseType(Integer id, String name, ForeignCollection<Course> courses) {
        this.id = id;
        this.name = name;
        this.courses = courses;
    }
}
