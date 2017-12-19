package com.idiotnation.raspored.Services;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.idiotnation.raspored.Models.LessonCell;
import com.idiotnation.raspored.Presenters.MainPresenter;
import com.idiotnation.raspored.Utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WearListenerService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient = null;
    private boolean nodeConnected = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(Utils.WEAR_GET_PATH)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!nodeConnected) {
                        mGoogleApiClient.blockingConnect(Utils.GOOGLEAPICLIENTTIMEOUT_S, TimeUnit.SECONDS);
                    }
                    if (!nodeConnected) {
                        return;
                    }
                    if (mGoogleApiClient.isConnected()) {
                        MainPresenter presenter = new MainPresenter();
                        presenter.start(null, getApplicationContext());

                        PutDataMapRequest dataMap = PutDataMapRequest.create(Utils.WEAR_UPDATE_PATH);
                        fillDataMap(presenter.getRaspored(), ByteBuffer.wrap(messageEvent.getData()).getInt(), dataMap.getDataMap());
                        PutDataRequest request = dataMap.asPutDataRequest();
                        request.setUrgent();

                        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                                    @Override
                                    public void onResult(DataApi.DataItemResult dataItemResult) {
                                    }
                                });
                    }
                }
            }).start();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        nodeConnected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        nodeConnected = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        nodeConnected = false;
    }

    private void fillDataMap(List<List<LessonCell>> columns, int index, DataMap dataMap) {
        List<LessonCell> lessons = Utils.shrinkList(columns);
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
        if (index == -10) {
            for (int i = 0; i < lessons.size(); i++) {
                LessonCell lessonCell = lessons.get(i);
                if (lessonCell.getStart().isAfterNow()) {
                    dataMap.putString(Utils.WEAR_CONTENT_KEY, lessonCell.getText());
                    dataMap.putString(Utils.WEAR_TIME_KEY, getDayOfWeek(lessonCell.getStart()) + " " + timeFormatter.print(lessonCell.getStart()) + " - " + timeFormatter.print(lessonCell.getEnd()));
                    dataMap.putInt(Utils.WEAR_INDEX_KEY, i);
                    break;
                }
            }
        } else {
            if (index < 0) {
                index = lessons.size() - 1;
            }
            if (index >= lessons.size()) {
                index = 0;
            }
            LessonCell lessonCell = lessons.get(index);
            dataMap.putString(Utils.WEAR_CONTENT_KEY, lessonCell.getText());
            dataMap.putString(Utils.WEAR_TIME_KEY, getDayOfWeek(lessonCell.getStart()) + " " + timeFormatter.print(lessonCell.getStart()) + " - " + timeFormatter.print(lessonCell.getEnd()));
            dataMap.putInt(Utils.WEAR_INDEX_KEY, index);
        }
        if (dataMap.isEmpty()) {
            dataMap.putString(Utils.WEAR_CONTENT_KEY, "Nema predavanja");
            dataMap.putString(Utils.WEAR_TIME_KEY, "");
            dataMap.putInt(Utils.WEAR_INDEX_KEY, -1);
        }
        dataMap.putString("ts", new DateTime().toString());
    }

    private String getDayOfWeek(DateTime date) {
        switch (date.getDayOfWeek()) {
            case 7:
                return "Nedjelja";
            case 1:
                return "Ponedjeljak";
            case 2:
                return "Utorak";
            case 3:
                return "Srijeda";
            case 4:
                return "Četvrtak";
            case 5:
                return "Petak";
            case 6:
                return "Subota";
            default:
                return "";
        }
    }
}
