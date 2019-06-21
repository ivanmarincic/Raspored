package com.idiotnation.raspored.views;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.FragmentManager;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.contracts.SettingsContract;
import com.idiotnation.raspored.dialogs.CourseSelectionDialog;
import com.idiotnation.raspored.fragments.SettingsAppointmentsFilterFragment;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.dto.CourseDto;
import com.idiotnation.raspored.models.dto.FilteredCourseDto;
import com.idiotnation.raspored.models.dto.PartialCourseDto;
import com.idiotnation.raspored.models.dto.SettingsDto;
import com.idiotnation.raspored.presenters.SettingsPresenter;

import java.sql.SQLException;
import java.util.List;

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
    private FragmentManager fragmentManager;

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
            if (presenter.getSettings().getSelectedCourse() != null) {
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
        presenter.loadSettings();
    }

    @Override
    public void loadSettings(SettingsDto settings) {
        final CourseDto selectedCourse = settings.getSelectedCourse();
        courseSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CourseSelectionDialog courseSelectionDialog = new CourseSelectionDialog(SettingsView.this, selectedCourse != null ? selectedCourse.getId() : -1, -1);
                courseSelectionDialog.setOnSelectListener(new CourseSelectionDialog.OnSelectListener() {
                    @Override
                    public void onSelect(CourseDto course) {
                        presenter.setSelectedCourse(course);
                        courseSelectionValue.setText(course.getName());
                    }
                });
                courseSelectionDialog.show();
            }
        });
        if (selectedCourse != null) {
            courseSelectionValue.setText(selectedCourse.getName());
        } else {
            courseSelectionValue.setText(getResources().getString(R.string.settings_view_list_value_course_default));
        }
        notificationsSelectionValue.setChecked(settings.getSyncNotifications());
        notificationsSelectionValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                presenter.setSyncNotifications(isChecked);
            }
        });
        autoSyncSelectionValue.setChecked(settings.getSyncAutomatically());
        autoSyncSelectionValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                presenter.setSyncAutomatically(isChecked);
            }
        });
        calendarSyncSelectionValue.setChecked(settings.getSyncCalendar());
        calendarSyncSelectionValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                presenter.setSyncCalendar(isChecked);
            }
        });
    }

    @Override
    public void loadPartials(SettingsDto settings, final List<PartialCourseDto> partials) {
        final CourseDto selectedCourse = settings.getSelectedCourse();
        final CourseDto partialCourse = settings.getPartialCourse();
        partialSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsAppointmentsFilterFragment fragment = new SettingsAppointmentsFilterFragment();
                Bundle fragmentArguments = new Bundle();
                fragmentArguments.putParcelable(SettingsAppointmentsFilterFragment.SELECTED_COURSE, partialCourse);
                fragmentArguments.putParcelable(SettingsAppointmentsFilterFragment.FILTERED_COURSE, selectedCourse);
                fragmentArguments.putString(SettingsAppointmentsFilterFragment.TITLE, getResources().getString(R.string.settings_view_list_value_partial));
                fragmentArguments.putString(SettingsAppointmentsFilterFragment.EMPTY_TEXT, getResources().getString(R.string.settings_view_list_value_partial_empty));
                fragmentArguments.putString(SettingsAppointmentsFilterFragment.BUTTON_TEXT, getResources().getString(R.string.settings_view_list_value_partial_add));
                fragmentArguments.putStringArrayList(SettingsAppointmentsFilterFragment.LIST_VALUES, Utils.listToStringList(partials));
                fragment.setArguments(fragmentArguments);
                fragment.setOnActionListener(new SettingsAppointmentsFilterFragment.OnActionListener() {
                    @Override
                    public void onAdded(String item, CourseDto course) {
                        presenter.addPartialAppointment(course, item);
                    }

                    @Override
                    public void onRemoved(String item, CourseDto course) {
                        presenter.removePartialAppointment(course, item);
                    }

                    @Override
                    public void onCourseSelected(CourseDto course) {
                        presenter.setPartialCourse(course);
                    }

                    @Override
                    public void onFinished(List<String> list, CourseDto course) {

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
        if (partials.size() > 0) {
            partialSelectionValue.setText(Utils.listToString(partials));
        }
    }

    @Override
    public void loadFiltered(SettingsDto settings, final List<FilteredCourseDto> filtered) {
        final CourseDto selectedCourse = settings.getSelectedCourse();
        blockedSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsAppointmentsFilterFragment fragment = new SettingsAppointmentsFilterFragment();
                Bundle fragmentArguments = new Bundle();
                fragmentArguments.putParcelable(SettingsAppointmentsFilterFragment.SELECTED_COURSE, selectedCourse);
                fragmentArguments.putString(SettingsAppointmentsFilterFragment.TITLE, getResources().getString(R.string.settings_view_list_value_blocked));
                fragmentArguments.putString(SettingsAppointmentsFilterFragment.EMPTY_TEXT, getResources().getString(R.string.settings_view_list_value_blocked_empty));
                fragmentArguments.putString(SettingsAppointmentsFilterFragment.BUTTON_TEXT, getResources().getString(R.string.settings_view_list_value_blocked_add));
                fragmentArguments.putBoolean(SettingsAppointmentsFilterFragment.HIDE_COURSE_SELECTION, true);
                fragmentArguments.putStringArrayList(SettingsAppointmentsFilterFragment.LIST_VALUES, Utils.listToStringList(filtered));
                fragment.setArguments(fragmentArguments);
                fragment.setOnActionListener(new SettingsAppointmentsFilterFragment.OnActionListener() {
                    @Override
                    public void onAdded(String item, CourseDto course) {
                        presenter.addFilteredAppointment(item);
                    }

                    @Override
                    public void onRemoved(String item, CourseDto course) {
                        presenter.removeFilteredAppointment(item);
                    }

                    @Override
                    public void onCourseSelected(CourseDto course) {
                    }

                    @Override
                    public void onFinished(List<String> list, CourseDto course) {
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
        if (filtered.size() > 0) {
            blockedSelectionValue.setText(Utils.listToString(filtered));
        }
    }

    @Override
    public void hideLoading() {
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
            presenter.syncWithCalendar();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == Utils.PERMISSIONS_READ_WRITE_CALENDAR) {
            calendarSyncSelectionValue.setChecked(false);
        }
    }
}
