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
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editWorkTime;
    private EditText editRestTime;
    private Button btnToggleService;
    private Button btnExport;
    private Button btnImport;
    private LinearLayout layoutHistoryList;
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

        btnExport = findViewById(R.id.btnExport);
        btnImport = findViewById(R.id.btnImport);
        layoutHistoryList = findViewById(R.id.layoutHistoryList);

        btnExport.setOnClickListener(v -> exportData());
        btnImport.setOnClickListener(v -> importData());

        loadHistoryList();

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
        loadHistoryList();
    }

    private void loadHistoryList() {
        layoutHistoryList.removeAllViews();
        try {
            String historyJson = prefs.getString("usageHistory", "{}");
            JSONObject history = new JSONObject(historyJson);
            ArrayList<String> dates = new ArrayList<>();
            Iterator<String> keys = history.keys();
            while (keys.hasNext()) {
                dates.add(keys.next());
            }
            Collections.sort(dates, Collections.reverseOrder());
            
            int count = 0;
            for (String date : dates) {
                if (count >= 30) break;
                long millis = history.getLong(date);
                long hours = millis / 3600000;
                long minutes = (millis % 3600000) / 60000;
                
                String[] parts = date.split("-");
                String displayDate = date;
                if (parts.length == 3) {
                    displayDate = parts[2] + "/" + parts[1] + "/" + parts[0].substring(2);
                }
                
                TextView tv = new TextView(this);
                tv.setText(displayDate + ": " + hours + "h " + minutes + "m");
                tv.setTextSize(16);
                tv.setPadding(0, 8, 0, 8);
                layoutHistoryList.addView(tv);
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportData() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "EyeCare_UsageHistory.json");
        startActivityForResult(intent, 1001);
    }

    private void importData() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, 1002);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (requestCode == 1001) { // Export
                try {
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    if (os != null) {
                        String historyJson = prefs.getString("usageHistory", "{}");
                        os.write(historyJson.getBytes());
                        os.close();
                        Toast.makeText(this, "Exported successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == 1002) { // Import
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    if (is != null) {
                        java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                        String json = scanner.hasNext() ? scanner.next() : "";
                        is.close();
                        new JSONObject(json); // Validate
                        prefs.edit().putString("usageHistory", json).apply();
                        loadHistoryList();
                        Toast.makeText(this, "Imported successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Import failed: Invalid file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
