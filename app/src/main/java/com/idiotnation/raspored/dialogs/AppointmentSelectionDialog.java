package com.idiotnation.raspored.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.adapters.ArrayListAdapter;
import com.idiotnation.raspored.custom.MaterialDialog;
import com.idiotnation.raspored.dataaccess.api.AppointmentService;
import com.idiotnation.raspored.dataaccess.api.ServiceGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class AppointmentSelectionDialog extends MaterialDialog {

    @BindView(R.id.settings_view_appointment_selection_list)
    RecyclerView list;

    @BindView(R.id.settings_view_appointment_selection_progress)
    ContentLoadingProgressBar progressBar;

    private ArrayListAdapter appointmentsAdapter;
    private OnSelectListener listener;
    private Integer selectedCourse;

    public AppointmentSelectionDialog(Activity activity, Integer selectedCourse) {
        super(activity);
        this.selectedCourse = selectedCourse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view_appointment_selection_dialog);
        setTitle(getContext().getResources().getString(R.string.settings_view_course_selection));
        ButterKnife.bind(this);
        progressBar.show();
        appointmentsAdapter = new ArrayListAdapter(new ArrayList<String>());
        appointmentsAdapter.setItemOnSelectListener(new ArrayListAdapter.ItemOnSelectListener() {
            @Override
            public void onSelect(Object item) {
                if (listener != null) {
                    listener.onSelect(item.toString());
                    dismiss();
                }
            }
        });
        list.setAdapter(appointmentsAdapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        ServiceGenerator.createService(AppointmentService.class)
                .getByCourse(selectedCourse)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<String> strings) {
                        appointmentsAdapter.setList(strings);
                        appointmentsAdapter.notifyDataSetChanged();
                        progressBar.hide();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof IOException) {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.request_error_internet), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.request_error_internal), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public interface OnSelectListener {
        void onSelect(String appointment);
    }

    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }
}
