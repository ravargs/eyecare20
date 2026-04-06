package com.eyecare.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class ScreenTimeService extends Service {
    private static final String CHANNEL_ID = "EyeCareTrackingChannel";
    private boolean isScreenOn = true;
    private long startTime;
    private int workTimeMillis;
    private Handler handler = new Handler();
    private OverlayManager overlayManager;

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                isScreenOn = true;
                startTimer();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                isScreenOn = false;
                stopTimer();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        overlayManager = new OverlayManager(this);

        SharedPreferences prefs = getSharedPreferences("EyeCarePrefs", MODE_PRIVATE);
        workTimeMillis = prefs.getInt("workTime", 20) * 60 * 1000;
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);
        
        startTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Eye Care Tracking")
                .setContentText("Monitoring screen time")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
        
        startForeground(1, notification);
        return START_STICKY;
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
        handler.removeCallbacks(timerRunnable);
        handler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        handler.removeCallbacks(timerRunnable);
    }

    private boolean isUserInCall() {
        android.media.AudioManager audioManager = (android.media.AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int mode = audioManager.getMode();
            if (mode == android.media.AudioManager.MODE_IN_CALL || mode == android.media.AudioManager.MODE_IN_COMMUNICATION) {
                return true;
            }
        }
        
        try {
            android.telephony.TelephonyManager telephonyManager = (android.telephony.TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int state = telephonyManager.getCallState();
                if (state == android.telephony.TelephonyManager.CALL_STATE_OFFHOOK || state == android.telephony.TelephonyManager.CALL_STATE_RINGING) {
                    return true;
                }
            }
        } catch (SecurityException e) {
            // Ignore if READ_PHONE_STATE permission is missing
        }
        
        return false;
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isScreenOn) {
                if (isUserInCall()) {
                    // Reset timer during the call so it waits another full cycle after call ends
                    startTime = System.currentTimeMillis();
                    if (overlayManager.isShowing()) {
                        overlayManager.removeOverlay();
                    }
                } else {
                    if (!overlayManager.isShowing()) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        if (elapsed >= workTimeMillis) {
                            overlayManager.showOverlay();
                            startTime = System.currentTimeMillis(); 
                        }
                    }
                }
                handler.postDelayed(this, 1000); 
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenReceiver);
        stopTimer();
        overlayManager.removeOverlay();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
