package com.example.musicservicedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        Button btnForeground = findViewById(R.id.btnForeground);
        Button btnBackground = findViewById(R.id.btnBackground);
        Button btnStop = findViewById(R.id.btnStop);

        // Xin quyền Notification (bắt buộc cho Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        btnForeground.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.setAction(MusicService.ACTION_START_FOREGROUND);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            tvStatus.setText("Trạng thái: Đang phát (Foreground Service)");
            Toast.makeText(this, "Started Foreground Service", Toast.LENGTH_SHORT).show();
        });

        btnBackground.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.setAction(MusicService.ACTION_START_BACKGROUND);
            startService(intent);
            tvStatus.setText("Trạng thái: Đang phát (Background Service)");
            Toast.makeText(this, "Started Background Service", Toast.LENGTH_SHORT).show();
        });

        btnStop.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.setAction(MusicService.ACTION_STOP);
            startService(intent);
            tvStatus.setText("Trạng thái: Đã dừng");
            Toast.makeText(this, "Stopped Service", Toast.LENGTH_SHORT).show();
        });
    }
}
