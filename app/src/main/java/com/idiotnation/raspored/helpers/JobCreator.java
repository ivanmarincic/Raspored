package com.idiotnation.raspored.helpers;

import com.evernote.android.job.Job;
import com.idiotnation.raspored.jobs.AppointmentNotificationJob;
import com.idiotnation.raspored.jobs.AutoUpdateJob;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class JobCreator implements com.evernote.android.job.JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case Utils.AUTO_UPDATE_JOB_TAG:
                return new AutoUpdateJob();
            case Utils.APPOINTEMENT_NOTIFICATIONS_JOB_TAG:
                return new AppointmentNotificationJob();
            default:
                return null;
        }
    }
}
