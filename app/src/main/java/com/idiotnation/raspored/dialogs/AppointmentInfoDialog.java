package com.idiotnation.raspored.dialogs;

import android.app.Activity;
import android.os.Bundle;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.custom.MaterialDialog;
import com.idiotnation.raspored.models.dto.AppointmentDto;

import org.joda.time.DateTimeZone;

import androidx.appcompat.widget.AppCompatTextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AppointmentInfoDialog extends MaterialDialog {

    @BindView(R.id.main_view_appointment_info_name)
    AppCompatTextView nameView;

    @BindView(R.id.main_view_appointment_info_details)
    AppCompatTextView detailsView;

    @BindView(R.id.main_view_appointment_info_classroom)
    AppCompatTextView classroomView;

    @BindView(R.id.main_view_appointment_info_time)
    AppCompatTextView timeView;

    @BindView(R.id.main_view_appointment_info_lecturer)
    AppCompatTextView lecturerView;

    @OnClick(R.id.main_view_appointment_info_block)
    public void block() {
        if (listener != null) {
            listener.onBlock(appointment);
            dismiss();
        }
    }

    private AppointmentDto appointment;
    private OnItemBlocked listener;

    public AppointmentInfoDialog(Activity activity, AppointmentDto appointment) {
        super(activity);
        this.appointment = appointment;
        setHeightPercent(-1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view_appointment_info_dialog);
        setTitle(getContext().getResources().getString(R.string.main_view_appointment_info_dialog_title));
        ButterKnife.bind(this);
        if (appointment != null) {
            nameView.setText(appointment.getName());
            detailsView.setText(appointment.getDetails());
            classroomView.setText(appointment.getClassroom());
            timeView.setText(appointment.getStart().withZone(DateTimeZone.getDefault()).toString("HH:mm") + " - " + appointment.getEnd().withZone(DateTimeZone.getDefault()).toString("HH:mm"));
            lecturerView.setText(appointment.getLecturer());
        }
    }

    public interface OnItemBlocked {
        void onBlock(AppointmentDto appointmentDto);
    }

    public void setOnItemBlockedListener(OnItemBlocked listener) {
        this.listener = listener;
    }
}
