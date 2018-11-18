package com.idiotnation.raspored.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.adapters.RemovableArrayListAdapter;
import com.idiotnation.raspored.dialogs.AppointmentSelectionDialog;
import com.idiotnation.raspored.dialogs.CourseSelectionDialog;
import com.idiotnation.raspored.models.dto.CourseDto;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SettingsAppointmentsFilterFragment extends Fragment {

    public static final String TITLE = "FRAGMENT_TITLE";
    public static final String EMPTY_TEXT = "EMPTY_TEXT";
    public static final String BUTTON_TEXT = "BUTTON_TEXT";
    public static final String LIST_VALUES = "LIST_VALUES";
    public static final String SELECTED_COURSE = "SELECTED_COURSE";
    public static final String SELECTED_COURSE_NAME = "SELECTED_COURSE_NAME";
    public static final String FILTERED_COURSE = "FILTERED_COURSE";
    public static final String HIDE_COURSE_SELECTION = "HIDE_COURSE_SELECTION";

    public interface OnFinishListener {
        void onFinish(List<String> list, Integer course, String courseName);
    }

    @BindView(R.id.settings_view_appointments_filter_toolbar_title)
    AppCompatTextView titleView;

    @BindView(R.id.settings_view_appointments_filter_add)
    MaterialButton addButton;

    @BindView(R.id.settings_view_appointments_filter_list)
    RecyclerView listView;

    @BindView(R.id.settings_view_appointments_filter_list_empty)
    AppCompatTextView emptyView;

    @BindView(R.id.settings_view_appointments_filter_course_select)
    View courseSelect;

    @BindView(R.id.settings_view_appointments_filter_course_value)
    AppCompatTextView courseSelectValue;

    @OnClick(R.id.settings_view_appointments_filter_add)
    public void add() {
        AppointmentSelectionDialog dialog = new AppointmentSelectionDialog(getActivity(), selectedCourse);
        dialog.setOnSelectListener(new AppointmentSelectionDialog.OnSelectListener() {
            @Override
            public void onSelect(String appointment) {
                if (!list.contains(appointment)) {
                    list.add(appointment);
                    listAdapter.notifyItemInserted(list.size() - 1);
                }
                if (list.size() > 0) {
                    emptyView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
        dialog.show();
    }

    @OnClick(R.id.settings_view_appointments_filter_course_select)
    public void courseSelection() {
        CourseSelectionDialog dialog = new CourseSelectionDialog(getActivity(), selectedCourse, filteredCourse);
        dialog.setOnSelectListener(new CourseSelectionDialog.OnSelectListener() {
            @Override
            public void onSelect(CourseDto course) {
                selectedCourse = course.getId();
                selectedCourseName = course.getName();
                courseSelectValue.setText(selectedCourseName);
                addButton.setEnabled(selectedCourse != -1);
            }
        });
        dialog.show();
    }

    @OnClick(R.id.settings_view_appointments_filter_toolbar_back)
    public void back() {
        if (listener != null && list != null) {
            listener.onFinish(list, selectedCourse, selectedCourseName);
        }
        getActivity().onBackPressed();
    }

    private List<String> list;
    private RemovableArrayListAdapter listAdapter;
    private OnFinishListener listener;
    private Integer filteredCourse = -1;
    private Integer selectedCourse = -1;
    private String selectedCourseName = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.settings_view_appointments_filter_fragment, container, false);
        ButterKnife.bind(this, rootView);
        Bundle arguments = getArguments();
        if (arguments != null) {
            titleView.setText(arguments.getString(TITLE, ""));
            addButton.setText(arguments.getString(BUTTON_TEXT, ""));
            emptyView.setText(arguments.getString(EMPTY_TEXT, ""));
            filteredCourse = arguments.getInt(FILTERED_COURSE, -1);
            selectedCourse = arguments.getInt(SELECTED_COURSE, -1);
            selectedCourseName = arguments.getString(SELECTED_COURSE_NAME, null);
            if (selectedCourseName != null) {
                courseSelectValue.setText(selectedCourseName);
            }
            list = arguments.getStringArrayList(LIST_VALUES);
            listAdapter = new RemovableArrayListAdapter(list);
            listView.setAdapter(listAdapter);
            listView.setLayoutManager(new LinearLayoutManager(getContext()));
            if (arguments.getBoolean(HIDE_COURSE_SELECTION, false)) {
                courseSelect.setVisibility(View.GONE);
            }
            if (list.size() > 0) {
                emptyView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.VISIBLE);
            }
            addButton.setEnabled(selectedCourse != -1);
        }
        return rootView;
    }

    public void setOnFinishListener(OnFinishListener listener) {
        this.listener = listener;
    }
}
