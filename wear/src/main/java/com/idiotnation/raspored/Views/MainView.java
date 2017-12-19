package com.idiotnation.raspored.Views;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.idiotnation.raspored.Contracts.MainContract;
import com.idiotnation.raspored.Presenters.MainPresenter;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainView extends WearableActivity implements MainContract.View, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        CapabilityApi.CapabilityListener {

    MainPresenter presenter;
    GoogleApiClient mGoogleApiClient;
    String nodeConnected = "";
    int currentLesson = -10;

    // Initialization
    @BindView(R.id.refreshLayout)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.text_content)
    TextView contentView;

    @BindView(R.id.text_time)
    TextView timeView;

    @BindView(R.id.button_up)
    ImageView buttonUp;

    @BindView(R.id.button_down)
    ImageView buttonDown;

    @OnClick(R.id.button_up)
    public void previous(View view) {
        buttonUp.setEnabled(false);
        if (currentLesson >= 0) {
            presenter.sendMessage(mGoogleApiClient, nodeConnected, Utils.WEAR_GET_PATH, --currentLesson);
        } else {
            presenter.sendMessage(mGoogleApiClient, nodeConnected, Utils.WEAR_GET_PATH, currentLesson);
        }
    }

    @OnClick(R.id.button_down)
    public void next(View view) {
        buttonDown.setEnabled(false);
        if (currentLesson >= 0) {
            presenter.sendMessage(mGoogleApiClient, nodeConnected, Utils.WEAR_GET_PATH, ++currentLesson);
        } else {
            presenter.sendMessage(mGoogleApiClient, nodeConnected, Utils.WEAR_GET_PATH, currentLesson);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        presenter = new MainPresenter();
        refreshLayout.setRefreshing(true);
        presenter.start(this, getApplicationContext());
        setAmbientEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.CapabilityApi.addListener(
                mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);
        presenter.setupCapability(mGoogleApiClient, Utils.GET_RASPORED_CAPAILITY_NAME);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (Utils.WEAR_UPDATE_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    updateData(dataMapItem.getDataMap());
                }

            }
        }
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {

    }

    @Override
    public void initialize() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void updateData(DataMap dataMap) {
        if (dataMap != null) {
            if (!dataMap.isEmpty()) {
                buttonUp.setEnabled(true);
                buttonDown.setEnabled(true);
                currentLesson = dataMap.getInt(Utils.WEAR_INDEX_KEY);
                if (currentLesson >= 0) {
                    contentView.setText(dataMap.getString(Utils.WEAR_CONTENT_KEY));
                    timeView.setText(dataMap.getString(Utils.WEAR_TIME_KEY));
                } else {
                    if (currentLesson == -1) {
                        contentView.setText("Nema predavanja");
                        timeView.setText("");
                    }
                    if (currentLesson == -2) {
                        contentView.setText("");
                        timeView.setText("");
                        Toast.makeText(getApplicationContext(), "Kraj raspored", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void setConnectedNodeId(String nodeId) {
        nodeConnected = nodeId;
        presenter.sendMessage(mGoogleApiClient, nodeConnected, Utils.WEAR_GET_PATH, currentLesson);
    }
}
