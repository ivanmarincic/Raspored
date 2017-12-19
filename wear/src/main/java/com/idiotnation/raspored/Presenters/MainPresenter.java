package com.idiotnation.raspored.Presenters;


import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.idiotnation.raspored.Contracts.MainContract;

import java.nio.ByteBuffer;
import java.util.Set;

public class MainPresenter implements MainContract.Presenter {

    MainContract.View view;
    Context context;

    @Override
    public void start(MainContract.View view, Context context) {
        this.view = view;
        this.context = context;
        if (view != null) {
            view.initialize();
        }
    }

    @Override
    public void setupCapability(final GoogleApiClient mGoogleApiClient, final String capabilityName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CapabilityApi.GetCapabilityResult result =
                        Wearable.CapabilityApi.getCapability(
                                mGoogleApiClient, capabilityName,
                                CapabilityApi.FILTER_REACHABLE).await();
                view.setConnectedNodeId(pickBestNodeId(result.getCapability().getNodes()));
            }
        }).start();
    }

    @Override
    public void sendMessage(GoogleApiClient mGoogleApiClient, String nodeId, String messagePath, int currentLesson) {
        if (nodeId != null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId,
                    messagePath, ByteBuffer.allocate(4).putInt(currentLesson).array()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                        }
                    }
            );
        }
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

}
