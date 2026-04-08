package com.eyecare.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editWorkTime;
    private EditText editRestTime;
    private Button btnToggleService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("EyeCarePrefs", MODE_PRIVATE);

        editWorkTime = findViewById(R.id.editWorkTime);
        editRestTime = findViewById(R.id.editRestTime);
        btnToggleService = findViewById(R.id.btnToggleService);

        int workTimeInfo = prefs.getInt("workTime", 20);
        int restTimeInfo = prefs.getInt("restTime", 20);

        editWorkTime.setText(String.valueOf(workTimeInfo));
        editRestTime.setText(String.valueOf(restTimeInfo));

        updateButtonState();

        btnToggleService.setOnClickListener(v -> {
            if (isServiceRunning(ScreenTimeService.class)) {
                stopServiceLocal();
            } else {
                if (checkPermissions()) {
                    startServiceLocal();
                }
            }
        });
    }

    private boolean checkPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            Toast.makeText(this, "Please grant overlay permission and try again", Toast.LENGTH_LONG).show();
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                return false;
            }
        }
        return true;
    }

    private void startServiceLocal() {
        savePrefs();
        Intent serviceIntent = new Intent(this, ScreenTimeService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        btnToggleService.postDelayed(this::updateButtonState, 500);
    }

    private void stopServiceLocal() {
        stopService(new Intent(this, ScreenTimeService.class));
        btnToggleService.postDelayed(this::updateButtonState, 500);
    }

    private void savePrefs() {
        int workTime = 20;
        int restTime = 20;
        try {
            workTime = Integer.parseInt(editWorkTime.getText().toString());
            restTime = Integer.parseInt(editRestTime.getText().toString());
        } catch (NumberFormatException ignored) {}

        prefs.edit()
            .putInt("workTime", workTime)
            .putInt("restTime", restTime)
            .apply();
    }

    private void updateButtonState() {
        if (isServiceRunning(ScreenTimeService.class)) {
            btnToggleService.setText(R.string.stop_service);
            btnToggleService.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E53935")));
        } else {
            btnToggleService.setText(R.string.start_service);
            btnToggleService.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#43A047")));
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonState();
    }
}
