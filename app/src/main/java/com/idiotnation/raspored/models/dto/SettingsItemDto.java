package com.idiotnation.raspored.models.dto;

public class SettingsItemDto {

    public static final String SETTINGS_TYPE_COURSE = "SETTINGS_SELECTED_COURSE";
    public static final String SETTINGS_TYPE_PARTIAL = "SETTINGS_PARTIAL_APPOINTMENTS";
    public static final String SETTINGS_TYPE_PARTIAL_COURSE = "SETTINGS_PARTIAL_COURSE";
    public static final String SETTINGS_TYPE_PARTIAL_COURSE_NAME = "SETTINGS_PARTIAL_COURSE_NAME";
    public static final String SETTINGS_TYPE_BLOCKED = "SETTINGS_BLOCKED_APPOINTMENTS";
    public static final String SETTINGS_TYPE_NOTIFICATIONS = "SETTINGS_NOTIFICATIONS";
    public static final String SETTINGS_TYPE_AUTOSYNC = "SETTINGS_AUTOSYNC";
    public static final String SETTINGS_TYPE_LAST_SYNC = "SETTINGS_LAST_SYNC";
    public static final String SETTINGS_TYPE_COURSE_LAST_SYNC = "SETTINGS_COURSE_LAST_SYNC";
    public static final String SETTINGS_TYPE_CALENDAR_SYNC = "SETTINGS_TYPE_CALENDAR_SYNC";
    public static final String SETTINGS_TYPE_CALENDAR_SYNC_ID = "SETTINGS_TYPE_CALENDAR_SYNC_ID";
    public static final String SETTINGS_TYPE_CALENDAR_SYNC_UUID = "SETTINGS_TYPE_CALENDAR_SYNC_UUID";

    Object value;
    String type;

    public Object getValue() {
        return value;
    }

    public <T> T getValue(Class<T> tClass) {
        return tClass.cast(value);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SettingsItemDto() {
    }

    public SettingsItemDto(Object value, String type) {
        this.value = value;
        this.type = type;
    }
}
