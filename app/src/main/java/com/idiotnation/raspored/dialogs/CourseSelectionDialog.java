package com.idiotnation.raspored.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.adapters.SettingsCoursesAdapter;
import com.idiotnation.raspored.custom.MaterialDialog;
import com.idiotnation.raspored.helpers.exceptions.ServerUnavailableException;
import com.idiotnation.raspored.models.dto.CourseDto;
import com.idiotnation.raspored.models.dto.CourseTypeDto;
import com.idiotnation.raspored.services.CourseService;
import com.idiotnation.raspored.views.SettingsView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class CourseSelectionDialog extends MaterialDialog {

    @BindView(R.id.settings_view_course_selection_list)
    RecyclerView list;

    @BindView(R.id.settings_view_course_selection_progress)
    ContentLoadingProgressBar progressBar;

    private SettingsCoursesAdapter coursesAdapter;
    private OnSelectListener listener;
    private Integer selectedCourse;
    private Integer filteredOutCourse;

    public CourseSelectionDialog(Activity activity, Integer selectedCourse, Integer filteredOutCourse) {
        super(activity);
        this.selectedCourse = selectedCourse;
        this.filteredOutCourse = filteredOutCourse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view_course_selection_dialog);
        setTitle(getContext().getResources().getString(R.string.settings_view_course_selection));
        ButterKnife.bind(this);
        progressBar.show();
        coursesAdapter = new SettingsCoursesAdapter(getContext(), new ArrayList<CourseTypeDto>(), selectedCourse, filteredOutCourse);
        coursesAdapter.setItemOnSelectListener(new SettingsCoursesAdapter.ItemOnSelectListener() {
            @Override
            public void onSelect(CourseDto item) {
                if (listener != null) {
                    listener.onSelect(item);
                    dismiss();
                }
            }
        });
        list.setAdapter(coursesAdapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        new CourseService()
                .syncLatest(((SettingsView) getActivity()).presenter.getCoursesFilter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<CourseTypeDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<CourseTypeDto> courseTypes) {
                        if (courseTypes != null) {
                            coursesAdapter.setList(courseTypes);
                            coursesAdapter.notifyDataSetChanged();
                            progressBar.hide();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof IOException) {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.request_error_internet), Toast.LENGTH_SHORT).show();
                        } else if (e instanceof ServerUnavailableException) {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.request_error_server), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.request_error_internal), Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();
                    }
                });
    }

    public interface OnSelectListener {
        void onSelect(CourseDto course);
    }

    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }
}
