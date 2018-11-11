package com.idiotnation.raspored.contracts;

import android.content.Context;

import com.idiotnation.raspored.models.dto.CourseFilterDto;
import com.idiotnation.raspored.models.dto.SettingsItemDto;

import java.sql.SQLException;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsContract {
    public interface View {
        void initialize();
        void loadSettings(HashMap<String, SettingsItemDto> settings);
        AppCompatActivity getActivity();
    }

    public interface Presenter {
        void start(View view, Context context) throws SQLException;
        void getSettings();
        void saveSettings(HashMap<String, SettingsItemDto> settings);
        CourseFilterDto getCoursesFilter();
        boolean getCalendarId();
    }
}
