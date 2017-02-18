package com.idiotnation.raspored.Recievers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.idiotnation.raspored.Services.NotificationUpdateService;

public class OnBootReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent updateIntent = new Intent(context, NotificationUpdateService.class);
            context.startService(updateIntent);
        }
    }
}
