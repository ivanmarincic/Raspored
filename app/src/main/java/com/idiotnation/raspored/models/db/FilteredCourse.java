package com.idiotnation.raspored.models.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "filtered_courses")
public class FilteredCourse {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private Integer id;

    @DatabaseField(columnName = "name", unique = true)
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

    public FilteredCourse() {
    }

    public FilteredCourse(Integer id) {
        this.id = id;
    }

    public FilteredCourse(String name) {
        this.name = name;
    }
}
