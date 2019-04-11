package com.idiotnation.raspored.views;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.contracts.SettingsContract;
import com.idiotnation.raspored.dialogs.CourseSelectionDialog;
import com.idiotnation.raspored.fragments.SettingsAppointmentsFilterFragment;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.dto.CourseDto;
import com.idiotnation.raspored.models.dto.SettingsItemDto;
import com.idiotnation.raspored.presenters.SettingsPresenter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

public class SettingsView extends AppCompatActivity implements SettingsContract.View, EasyPermissions.PermissionCallbacks {

    @BindView(R.id.settings_view_container)
    View container;

    @BindView(R.id.settings_view_toolbar)
    Toolbar toolbar;

    @BindView(R.id.settings_view_course_select)
    View courseSelection;

    @BindView(R.id.settings_view_course_select_value)
    AppCompatTextView courseSelectionValue;

    @BindView(R.id.settings_view_partial_select)
    View partialSelection;

    @BindView(R.id.settings_view_partial_select_value)
    AppCompatTextView partialSelectionValue;

    @BindView(R.id.settings_view_blocked_select)
    View blockedSelection;

    @BindView(R.id.settings_view_blocked_select_value)
    AppCompatTextView blockedSelectionValue;

    @BindView(R.id.settings_view_notifications_select_value)
    AppCompatCheckBox notificationsSelectionValue;

    @BindView(R.id.settings_view_autosync_select_value)
    AppCompatCheckBox autoSyncSelectionValue;

    @BindView(R.id.settings_view_calendar_sync_select_value)
    AppCompatCheckBox calendarSyncSelectionValue;

    @BindView(R.id.settings_view_progress)
    ContentLoadingProgressBar progressBar;

    @OnClick(R.id.settings_view_notifications_select)
    public void notificationsToggle() {
        notificationsSelectionValue.toggle();
    }

    @OnClick(R.id.settings_view_autosync_select)
    public void autoSyncToggle() {
        autoSyncSelectionValue.toggle();
    }

    @OnClick(R.id.settings_view_calendar_sync_select)
    public void calendarSyncToggle() {
        calendarSyncSelectionValue.toggle();
    }

    public SettingsContract.Presenter presenter;
    HashMap<String, SettingsItemDto> currentSettings;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view);
        ButterKnife.bind(this);
        presenter = new SettingsPresenter();
        try {
            presenter.start(this, getApplicationContext());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            presenter.saveSettings(currentSettings);
            if (currentSettings.get(SettingsItemDto.SETTINGS_TYPE_COURSE).getValue() != null) {
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, getResources().getString(R.string.settings_view_list_value_course_required), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void initialize() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        fragmentManager = getSupportFragmentManager();
        courseSelectionValue.setSelected(true);
        progressBar.show();
        presenter.getSettings();
    }

    @Override
    public void loadSettings(final HashMap<String, SettingsItemDto> settings) {
        currentSettings = settings;
        for (SettingsItemDto item : settings.values()) {
            switch (item.getType()) {
                case SettingsItemDto.SETTINGS_TYPE_COURSE:
                    courseSelection.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Integer courseId = currentSettings.get(SettingsItemDto.SETTINGS_TYPE_COURSE).getValue(Integer.class);
                            CourseSelectionDialog courseSelectionDialog = new CourseSelectionDialog(SettingsView.this, courseId, -1);
                            courseSelectionDialog.setOnSelectListener(new CourseSelectionDialog.OnSelectListener() {
                                @Override
                                public void onSelect(CourseDto course) {
                                    currentSettings.get(SettingsItemDto.SETTINGS_TYPE_COURSE).setValue(course.getId());
                                    currentSettings.get(SettingsItemDto.SETTINGS_TYPE_COURSE_NAME).setValue(course.getName());
                                    currentSettings.get(SettingsItemDto.SETTINGS_TYPE_COURSE_LAST_SYNC).setValue(DateTime.now().withZone(DateTimeZone.UTC));
                                    courseSelectionValue.setText(course.getName());
                                }
                            });
                            courseSelectionDialog.show();
                        }
                    });
                    if (item.getValue(Integer.class) != -1) {
                        courseSelectionValue.setText(currentSettings.get(SettingsItemDto.SETTINGS_TYPE_COURSE_NAME).getValue(String.class));
                    } else {
                        courseSelectionValue.setText(getResources().getString(R.string.settings_view_list_value_course_default));
                    }
                    break;
                case SettingsItemDto.SETTINGS_TYPE_PARTIAL:
                    partialSelection.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SettingsAppointmentsFilterFragment fragment = new SettingsAppointmentsFilterFragment();
                            Bundle fragmentArguments = new Bundle();
                            if (currentSettings.get(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE).getValue(Integer.class) != -1) {
                                Integer partialCourseId = currentSettings.get(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE).getValue(Integer.class);
                                String courseName = currentSettings.get(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE_NAME).getValue(String.class);
                                fragmentArguments.putInt(SettingsAppointmentsFilterFragment.SELECTED_COURSE, partialCourseId);
                                fragmentArguments.putString(SettingsAppointmentsFilterFragment.SELECTED_COURSE_NAME, courseName);
                            }
                            Integer courseId = currentSettings.get(SettingsItemDto.SETTINGS_TYPE_COURSE).getValue(Integer.class);
                            fragmentArguments.putInt(SettingsAppointmentsFilterFragment.FILTERED_COURSE, courseId);
                            fragmentArguments.putString(SettingsAppointmentsFilterFragment.TITLE, getResources().getString(R.string.settings_view_list_value_partial));
                            fragmentArguments.putString(SettingsAppointmentsFilterFragment.EMPTY_TEXT, getResources().getString(R.string.settings_view_list_value_partial_empty));
                            fragmentArguments.putString(SettingsAppointmentsFilterFragment.BUTTON_TEXT, getResources().getString(R.string.settings_view_list_value_partial_add));
                            fragmentArguments.putStringArrayList(SettingsAppointmentsFilterFragment.LIST_VALUES, new ArrayList<String>(currentSettings.get(SettingsItemDto.SETTINGS_TYPE_PARTIAL).getValue(List.class)));
                            fragment.setArguments(fragmentArguments);
                            fragment.setOnFinishListener(new SettingsAppointmentsFilterFragment.OnFinishListener() {
                                @Override
                                public void onFinish(List<String> list, Integer course, String courseName) {
                                    currentSettings.put(SettingsItemDto.SETTINGS_TYPE_PARTIAL, new SettingsItemDto(list, SettingsItemDto.SETTINGS_TYPE_PARTIAL));
                                    currentSettings.put(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE, new SettingsItemDto(course, SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE));
                                    currentSettings.put(SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE_NAME, new SettingsItemDto(courseName, SettingsItemDto.SETTINGS_TYPE_PARTIAL_COURSE_NAME));
                                    if (list.size() > 0) {
                                        partialSelectionValue.setText(Utils.listToString(list));
                                    } else {
                                        partialSelectionValue.setText(getResources().getString(R.string.settings_view_list_value_partial_default));
                                    }
                                }
                            });
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.settings_view_fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    });
                    if (item.getValue(List.class).size() > 0) {
                        partialSelectionValue.setText(Utils.listToString(item.getValue(List.class)));
                    }
                    break;
                case SettingsItemDto.SETTINGS_TYPE_BLOCKED:
                    blockedSelection.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SettingsAppointmentsFilterFragment fragment = new SettingsAppointmentsFilterFragment();
                            Bundle fragmentArguments = new Bundle();
                            Integer courseId = currentSettings.get(SettingsItemDto.SETTINGS_TYPE_COURSE).getValue(Integer.class);
                            fragmentArguments.putInt(SettingsAppointmentsFilterFragment.SELECTED_COURSE, courseId);
                            fragmentArguments.putString(SettingsAppointmentsFilterFragment.TITLE, getResources().getString(R.string.settings_view_list_value_blocked));
                            fragmentArguments.putString(SettingsAppointmentsFilterFragment.EMPTY_TEXT, getResources().getString(R.string.settings_view_list_value_blocked_empty));
                            fragmentArguments.putString(SettingsAppointmentsFilterFragment.BUTTON_TEXT, getResources().getString(R.string.settings_view_list_value_blocked_add));
                            fragmentArguments.putBoolean(SettingsAppointmentsFilterFragment.HIDE_COURSE_SELECTION, true);
                            fragmentArguments.putStringArrayList(SettingsAppointmentsFilterFragment.LIST_VALUES, new ArrayList<String>(currentSettings.get(SettingsItemDto.SETTINGS_TYPE_BLOCKED).getValue(List.class)));
                            fragment.setArguments(fragmentArguments);
                            fragment.setOnFinishListener(new SettingsAppointmentsFilterFragment.OnFinishListener() {
                                @Override
                                public void onFinish(List<String> list, Integer course, String courseName) {
                                    currentSettings.put(SettingsItemDto.SETTINGS_TYPE_BLOCKED, new SettingsItemDto(list, SettingsItemDto.SETTINGS_TYPE_BLOCKED));
                                    if (list.size() > 0) {
                                        blockedSelectionValue.setText(Utils.listToString(list));
                                    } else {
                                        blockedSelectionValue.setText(getResources().getString(R.string.settings_view_list_value_blocked_default));
                                    }
                                }
                            });
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.settings_view_fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    });
                    if (item.getValue(List.class).size() > 0) {
                        blockedSelectionValue.setText(Utils.listToString(item.getValue(List.class)));
                    }
                    break;
                case SettingsItemDto.SETTINGS_TYPE_NOTIFICATIONS:
                    notificationsSelectionValue.setChecked(item.getValue(Boolean.class));
                    notificationsSelectionValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            currentSettings.get(SettingsItemDto.SETTINGS_TYPE_NOTIFICATIONS).setValue(isChecked);
                            if (isChecked) {
                                presenter.scheduleAppointmentNotificationsJob();
                            } else {
                                presenter.cancelAppointmentNotificationsJob();
                            }
                        }
                    });
                    break;
                case SettingsItemDto.SETTINGS_TYPE_AUTOSYNC:
                    autoSyncSelectionValue.setChecked(item.getValue(Boolean.class));
                    autoSyncSelectionValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            currentSettings.get(SettingsItemDto.SETTINGS_TYPE_AUTOSYNC).setValue(isChecked);
                            if (isChecked) {
                                presenter.scheduleAutoUpdateJob();
                            } else {
                                presenter.cancelAutoUpdateJob();
                            }
                        }
                    });
                    break;
                case SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC:
                    calendarSyncSelectionValue.setChecked(item.getValue(Boolean.class));
                    calendarSyncSelectionValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                presenter.getCalendarId();
                            }
                            currentSettings.get(SettingsItemDto.SETTINGS_TYPE_CALENDAR_SYNC).setValue(isChecked);
                        }
                    });
                    break;
            }
        }
        progressBar.hide();
        container.setVisibility(View.VISIBLE);
    }

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == Utils.PERMISSIONS_READ_WRITE_CALENDAR) {
            presenter.getCalendarId();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == Utils.PERMISSIONS_READ_WRITE_CALENDAR) {
            calendarSyncSelectionValue.setChecked(false);
        }
    }
}
