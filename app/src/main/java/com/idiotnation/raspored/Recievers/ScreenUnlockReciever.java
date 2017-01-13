package com.idiotnation.raspored.Recievers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.idiotnation.raspored.Utils;
import com.idiotnation.raspored.Widget.RasporedWidgetProvider;

public class ScreenUnlockReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Intent updateIntent = new Intent(context, RasporedWidgetProvider.class);
            updateIntent.putExtra(Utils.WIDGET_INTENT, Utils.WIDGET_UPDATE);
            context.sendBroadcast(updateIntent);
        }
    }
}
