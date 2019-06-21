package com.idiotnation.raspored.models.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "partial_courses")
public class PartialCourse {
    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private Integer id;

    @DatabaseField(columnName = "course_id", foreign = true, foreignAutoRefresh = true, uniqueIndexName = "UQ_COURSE_NAME")
    private Course course;

    @DatabaseField(columnName = "name", uniqueIndexName = "UQ_COURSE_NAME")
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PartialCourse() {
    }

    public PartialCourse(Integer id) {
        this.id = id;
    }

    public PartialCourse(Course course, String name) {
        this.id = id;
        this.course = course;
        this.name = name;
    }
}
