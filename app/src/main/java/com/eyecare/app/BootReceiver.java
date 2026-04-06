package com.eyecare.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            "android.intent.action.LOCKED_BOOT_COMPLETED".equals(action) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            Intent serviceIntent = new Intent(context, ScreenTimeService.class);
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
