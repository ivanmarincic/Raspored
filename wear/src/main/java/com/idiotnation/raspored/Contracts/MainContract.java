package com.idiotnation.raspored.Contracts;


import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;

public class MainContract {

    public interface View {
        void initialize();

        void updateData(DataMap dataMap);

        void setConnectedNodeId(String nodeId);
    }

    public interface Presenter {
        void start(View view, Context context);

        void setupCapability(GoogleApiClient mGoogleApiClient, String capabilityName);

        void sendMessage(GoogleApiClient mGoogleApiClient, String nodeId, String messagePath, int currentLesson);
    }

}
