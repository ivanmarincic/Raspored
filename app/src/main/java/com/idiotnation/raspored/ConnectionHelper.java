package com.idiotnation.raspored;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.jsoup.Jsoup;

public class ConnectionHelper extends AsyncTask<Void, Void, Void> {

    finishListener finishListener;
    Context context;

    public ConnectionHelper(Context context){
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        finishListener.onFinish(checkActiveInternetConnection());
        return null;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean checkActiveInternetConnection() {
        if (isNetworkAvailable()) {
            try {
                Jsoup.connect("https://www.google.com").timeout(3000).get();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void setFinishListener(finishListener finishListener){
        this.finishListener = finishListener;
    }

    interface finishListener{
        void onFinish(boolean isConnectionEstablished);
    }
}