package com.idiotnation.raspored.models.dto;

import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.jpa.Appointment;

import org.joda.time.DateTime;


public class AppointmentDto {

    Integer id = null;
    String name = "";
    CourseDto course = null;
    String details = "";
    String classroom = "";
    String lecturer = "";
    DateTime start = new DateTime();
    DateTime end = new DateTime();

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

    public CourseDto getCourse() {
        return course;
    }

    public void setCourse(CourseDto course) {
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

    public AppointmentDto() {
    }

    public AppointmentDto(Integer id, String name, CourseDto course, String details, String classroom, String lecturer, DateTime start, DateTime end) {
        this.id = id;
        this.name = name;
        this.course = course;
        this.details = details;
        this.classroom = classroom;
        this.lecturer = lecturer;
        this.start = start;
        this.end = end;
    }

    public AppointmentDto(Appointment appointment) {
        this.id = appointment.getId();
        this.name = appointment.getName();
        this.details = appointment.getDetails();
        this.classroom = appointment.getClassroom();
        this.lecturer = appointment.getLecturer();
        this.start = appointment.getStart();
        this.end = appointment.getEnd();
        if (appointment.getCourse() != null) {
            this.course = new CourseDto(appointment.getCourse());
        }
    }

    public Appointment toPojo() {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setClassroom(classroom);
        appointment.setDetails(details);
        appointment.setEnd(end);
        appointment.setLecturer(lecturer);
        appointment.setStart(start);
        appointment.setName(name);
        if (appointment.getCourse() != null) {
            appointment.setCourse(course.toPojo());
        }
        return appointment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppointmentDto that = (AppointmentDto) o;
        return Utils.equals(name, that.name) &&
                Utils.equals(course, that.course) &&
                Utils.equals(details, that.details) &&
                Utils.equals(classroom, that.classroom) &&
                Utils.equals(lecturer, that.lecturer) &&
                Utils.equals(start, that.start) &&
                Utils.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Utils.hash(name, course, details, classroom, lecturer, start, end);
    }

    @Override
    public String toString() {
        return name;
    }
}