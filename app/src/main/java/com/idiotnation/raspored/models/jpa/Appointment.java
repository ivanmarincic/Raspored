package com.idiotnation.raspored.models.jpa;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

@DatabaseTable(tableName = "appointments")
public class Appointment {
    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private Integer id = null;

    @DatabaseField(columnName = "name")
    private String name = "";

    @DatabaseField(columnName = "course_id", foreign = true, foreignAutoRefresh = true)
    private Course course = new Course();

    @DatabaseField(columnName = "details")
    private String details = "";

    @DatabaseField(columnName = "classroom")
    private String classroom = "";

    @DatabaseField(columnName = "lecturer")
    private String lecturer = "";

    @DatabaseField(columnName = "start", dataType = DataType.DATE_TIME)
    private DateTime start = new DateTime();

    @DatabaseField(columnName = "end", dataType = DataType.DATE_TIME)
    private DateTime end = new DateTime();

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

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getLecturer() {
        return lecturer;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public Appointment() {
    }

    public Appointment(Integer id) {
        this.id = id;
    }

    public Appointment(Integer id, String name, Course course, String details, String classroom, String lecturer, DateTime start, DateTime end) {
        this.id = id;
        this.name = name;
        this.course = course;
        this.details = details;
        this.classroom = classroom;
        this.lecturer = lecturer;
        this.start = start;
        this.end = end;
    }
}
