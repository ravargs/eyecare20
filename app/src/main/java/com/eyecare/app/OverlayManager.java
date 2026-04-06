package com.eyecare.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayManager {
    private Context context;
    private WindowManager windowManager;
    private View overlayView;
    private TextView tvTimer;
    private boolean isOverlayShowing = false;
    private CountDownTimer countDownTimer;
    private int restTimeSeconds;

    public OverlayManager(Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void showOverlay() {
        if (isOverlayShowing) return;

        SharedPreferences prefs = context.getSharedPreferences("EyeCarePrefs", Context.MODE_PRIVATE);
        restTimeSeconds = prefs.getInt("restTime", 20);

        LayoutInflater inflater = LayoutInflater.from(context);
        overlayView = inflater.inflate(R.layout.overlay_layout, null);
        tvTimer = overlayView.findViewById(R.id.tvTimer);

        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        playSound();

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                windowManager.addView(overlayView, params);
                isOverlayShowing = true;
                startCountdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(restTimeSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (tvTimer != null) {
                    tvTimer.setText(String.valueOf(millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                removeOverlay();
                playSound();
            }
        }.start();
    }

    public boolean isShowing() {
        return isOverlayShowing;
    }

    public void removeOverlay() {
        if (!isOverlayShowing || overlayView == null) return;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                if (overlayView != null) {
                    windowManager.removeView(overlayView);
                }
                isOverlayShowing = false;
                overlayView = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void playSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
